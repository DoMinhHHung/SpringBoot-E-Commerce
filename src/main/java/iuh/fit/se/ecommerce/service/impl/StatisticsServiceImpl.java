package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.response.StatisticsResponse;
import iuh.fit.se.ecommerce.dto.response.StatisticsDetailResponse;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import iuh.fit.se.ecommerce.repository.*;
import iuh.fit.se.ecommerce.service.interfaces.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final List<OrderStatus> SUCCESS_STATUSES = List.of(
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );

    private static final Set<String> SUPPORTED_PERIODS = Set.of("DAY", "WEEK", "MONTH", "QUARTER", "YEAR");

    private final RevenueAuditRepository revenueAuditRepository;
    private final BuyerAuditRepository buyerAuditRepository;
    private final NewCustomerAuditRepository newCustomerAuditRepository;
    private final ProductSaleAuditRepository productSaleAuditRepository;
    
    // Keep old repositories for fallback if audit tables are empty
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    public StatisticsResponse getStatistics(String period) {
        String normalized = normalizePeriod(period);
        LocalDateTime start = resolveStart(normalized);
        LocalDateTime end = resolveEnd(normalized, start);

        // Query from audit tables (preferred) with fallback to original tables
        BigDecimal revenue = getRevenueFromAudit(start, end);
        Long buyerCount = getBuyerCountFromAudit(start, end);
        Long newCustomers = getNewCustomersFromAudit(start, end);
        Long productsSold = getProductsSoldFromAudit(start, end);

        return StatisticsResponse.builder()
                .period(normalized)
                .startDate(start)
                .endDate(end)
                .revenue(revenue)
                .buyerCount(buyerCount)
                .newCustomers(newCustomers)
                .productsSold(productsSold)
                .build();
    }
    
    private BigDecimal getRevenueFromAudit(LocalDateTime start, LocalDateTime end) {
        BigDecimal auditRevenue = revenueAuditRepository.sumRevenueByRecordedAtBetween(start, end);
        if (auditRevenue != null && auditRevenue.compareTo(BigDecimal.ZERO) > 0) {
            return auditRevenue;
        }
        // Fallback to original query if audit table is empty
        return Objects.requireNonNullElse(
                orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(SUCCESS_STATUSES, start, end),
                BigDecimal.ZERO
        );
    }
    
    private Long getBuyerCountFromAudit(LocalDateTime start, LocalDateTime end) {
        Long auditBuyers = buyerAuditRepository.countDistinctBuyersByRecordedAtBetween(start, end);
        if (auditBuyers != null && auditBuyers > 0) {
            return auditBuyers;
        }
        // Fallback to original query
        return Objects.requireNonNullElse(
                orderRepository.countDistinctUserByStatusAndCreatedAtBetween(SUCCESS_STATUSES, start, end),
                0L
        );
    }
    
    private Long getNewCustomersFromAudit(LocalDateTime start, LocalDateTime end) {
        Long auditCustomers = newCustomerAuditRepository.countByRegisteredAtBetween(start, end);
        if (auditCustomers != null && auditCustomers > 0) {
            return auditCustomers;
        }
        // Fallback to original query
        return Objects.requireNonNullElse(
                userRepository.countByCreatedAtBetween(start, end),
                0L
        );
    }
    
    private Long getProductsSoldFromAudit(LocalDateTime start, LocalDateTime end) {
        Long auditProducts = productSaleAuditRepository.sumQuantityByRecordedAtBetween(start, end);
        if (auditProducts != null && auditProducts > 0) {
            return auditProducts;
        }
        // Fallback to original query
        return Objects.requireNonNullElse(
                orderItemRepository.sumQuantityByOrderStatusAndCreatedAtBetween(SUCCESS_STATUSES, start, end),
                0L
        );
    }

    @Override
    public StatisticsResponse getStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        // Query from audit tables (preferred) with fallback to original tables
        BigDecimal revenue = getRevenueFromAudit(startDate, endDate);
        Long buyerCount = getBuyerCountFromAudit(startDate, endDate);
        Long newCustomers = getNewCustomersFromAudit(startDate, endDate);
        Long productsSold = getProductsSoldFromAudit(startDate, endDate);

        return StatisticsResponse.builder()
                .period("CUSTOM")
                .startDate(startDate)
                .endDate(endDate)
                .revenue(revenue)
                .buyerCount(buyerCount)
                .newCustomers(newCustomers)
                .productsSold(productsSold)
                .build();
    }

    private String normalizePeriod(String period) {
        String normalized = period == null ? "DAY" : period.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_PERIODS.contains(normalized)) {
            normalized = "DAY";
        }
        return normalized;
    }

    private LocalDateTime resolveStart(String period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case "WEEK" -> today.with(DayOfWeek.MONDAY).atStartOfDay();
            case "MONTH" -> today.withDayOfMonth(1).atStartOfDay();
            case "QUARTER" -> {
                int currentMonth = today.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                yield LocalDate.of(today.getYear(), quarterStartMonth, 1).atStartOfDay();
            }
            case "YEAR" -> LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
            default -> today.atStartOfDay();
        };
    }

    private LocalDateTime resolveEnd(String period, LocalDateTime start) {
        return switch (period) {
            case "WEEK" -> start.plusWeeks(1);
            case "MONTH" -> start.plusMonths(1);
            case "QUARTER" -> start.plusMonths(3);
            case "YEAR" -> start.plusYears(1);
            default -> start.plusDays(1);
        };
    }

    // ========== Methods cho StatisticsDetailResponse ==========

    @Override
    public StatisticsDetailResponse getStatisticsDetail(String period) {
        String normalized = normalizePeriod(period);
        LocalDateTime start = resolveStart(normalized);
        LocalDateTime end = resolveEnd(normalized, start);
        
        // Xác định granularity dựa trên period
        String granularity = determineGranularity(normalized, start, end);
        
        // Query dữ liệu chi tiết
        List<StatisticsDetailResponse.StatisticsDataPoint> dataPoints = 
            queryDetailedStatistics(start, end, granularity);
        
        return StatisticsDetailResponse.builder()
                .period(normalized)
                .startDate(start)
                .endDate(end)
                .granularity(granularity)
                .dataPoints(dataPoints)
                .build();
    }

    @Override
    public StatisticsDetailResponse getStatisticsDetailByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        
        // Xác định granularity dựa trên khoảng thời gian
        String granularity = determineGranularityForCustomRange(startDate, endDate);
        
        // Query dữ liệu chi tiết
        List<StatisticsDetailResponse.StatisticsDataPoint> dataPoints = 
            queryDetailedStatistics(startDate, endDate, granularity);
        
        return StatisticsDetailResponse.builder()
                .period("CUSTOM")
                .startDate(startDate)
                .endDate(endDate)
                .granularity(granularity)
                .dataPoints(dataPoints)
                .build();
    }

    private String determineGranularity(String period, LocalDateTime start, LocalDateTime end) {
        return switch (period) {
            case "DAY" -> "HOUR";      // Ngày: theo giờ (24 giờ)
            case "WEEK" -> "DAY";      // Tuần: theo ngày (7 ngày)
            case "MONTH" -> "DAY";     // Tháng: theo ngày (30/31 ngày)
            case "YEAR" -> "MONTH";    // Năm: theo tháng (12 tháng)
            default -> "DAY";
        };
    }

    private String determineGranularityForCustomRange(LocalDateTime start, LocalDateTime end) {
        long hours = ChronoUnit.HOURS.between(start, end);
        
        if (hours <= 24) {
            return "HOUR";      // <= 1 ngày: theo giờ
        } else if (hours <= 24 * 7) {
            return "DAY";       // <= 1 tuần: theo ngày
        } else if (hours <= 24 * 31) {
            return "DAY";       // <= 1 tháng: theo ngày
        } else {
            return "MONTH";     // <= 1 năm: theo tháng
        }
    }

    private List<StatisticsDetailResponse.StatisticsDataPoint> queryDetailedStatistics(
            LocalDateTime start, LocalDateTime end, String granularity) {
        
        // Tạo danh sách các khoảng thời gian (fill empty slots)
        List<TimeSlot> timeSlots = generateTimeSlots(start, end, granularity);
        
        // Query dữ liệu từ database
        Map<String, StatisticsDetailResponse.StatisticsDataPoint> dataMap = 
            queryStatisticsFromDatabase(start, end, granularity);
        
        // Merge và fill dữ liệu
        return timeSlots.stream()
                .map(slot -> {
                    StatisticsDetailResponse.StatisticsDataPoint point = dataMap.get(slot.getKey());
                    
                    if (point != null) {
                        return point;
                    } else {
                        // Tạo điểm dữ liệu rỗng
                        return StatisticsDetailResponse.StatisticsDataPoint.builder()
                                .label(slot.getLabel())
                                .startTime(slot.getStart())
                                .endTime(slot.getEnd())
                                .revenue(BigDecimal.ZERO)
                                .buyerCount(0L)
                                .newCustomers(0L)
                                .productsSold(0L)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    private List<TimeSlot> generateTimeSlots(LocalDateTime start, LocalDateTime end, String granularity) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime current = start;
        
        while (current.isBefore(end)) {
            LocalDateTime slotEnd = switch (granularity) {
                case "HOUR" -> current.plusHours(1);
                case "DAY" -> current.plusDays(1);
                case "MONTH" -> current.plusMonths(1);
                default -> current.plusDays(1);
            };
            
            if (slotEnd.isAfter(end)) {
                slotEnd = end;
            }
            
            String label = formatLabel(current, granularity);
            String key = formatKey(current, granularity);
            
            slots.add(new TimeSlot(key, label, current, slotEnd));
            current = slotEnd;
        }
        
        return slots;
    }

    private String formatLabel(LocalDateTime dateTime, String granularity) {
        return switch (granularity) {
            case "HOUR" -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            case "DAY" -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM"));
            case "MONTH" -> dateTime.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            default -> dateTime.toString();
        };
    }

    private String formatKey(LocalDateTime dateTime, String granularity) {
        return switch (granularity) {
            case "HOUR" -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00"));
            case "DAY" -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "MONTH" -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            default -> dateTime.toString();
        };
    }

    private Map<String, StatisticsDetailResponse.StatisticsDataPoint> queryStatisticsFromDatabase(
            LocalDateTime start, LocalDateTime end, String granularity) {
        
        Map<String, StatisticsDetailResponse.StatisticsDataPoint> dataMap = new HashMap<>();
        
        // Helper method để tạo hoặc lấy point
        java.util.function.Function<Object[], StatisticsDetailResponse.StatisticsDataPoint> getOrCreatePoint = (row) -> {
            String key = (String) row[0];
            LocalDateTime startTime = ((java.sql.Timestamp) row[1]).toLocalDateTime();
            LocalDateTime endTime = ((java.sql.Timestamp) row[2]).toLocalDateTime();
            
            StatisticsDetailResponse.StatisticsDataPoint point = dataMap.get(key);
            if (point == null) {
                point = StatisticsDetailResponse.StatisticsDataPoint.builder()
                        .label(formatLabel(startTime, granularity))
                        .startTime(startTime)
                        .endTime(endTime)
                        .revenue(BigDecimal.ZERO)
                        .buyerCount(0L)
                        .newCustomers(0L)
                        .productsSold(0L)
                        .build();
                dataMap.put(key, point);
            }
            return point;
        };
        
        // Query Revenue
        List<Object[]> revenueData = switch (granularity) {
            case "HOUR" -> revenueAuditRepository.sumRevenueByHour(start, end);
            case "DAY" -> revenueAuditRepository.sumRevenueByDay(start, end);
            case "MONTH" -> revenueAuditRepository.sumRevenueByMonth(start, end);
            default -> revenueAuditRepository.sumRevenueByDay(start, end);
        };
        
        for (Object[] row : revenueData) {
            if (row == null || row.length < 4) continue;
            StatisticsDetailResponse.StatisticsDataPoint point = getOrCreatePoint.apply(row);
            BigDecimal revenue = row[3] != null ? ((java.math.BigDecimal) row[3]) : BigDecimal.ZERO;
            point.setRevenue(revenue);
        }
        
        // Query Buyers
        List<Object[]> buyersData = switch (granularity) {
            case "HOUR" -> buyerAuditRepository.countBuyersByHour(start, end);
            case "DAY" -> buyerAuditRepository.countBuyersByDay(start, end);
            case "MONTH" -> buyerAuditRepository.countBuyersByMonth(start, end);
            default -> buyerAuditRepository.countBuyersByDay(start, end);
        };
        
        for (Object[] row : buyersData) {
            if (row == null || row.length < 4) continue;
            StatisticsDetailResponse.StatisticsDataPoint point = getOrCreatePoint.apply(row);
            Long buyerCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            point.setBuyerCount(buyerCount);
        }
        
        // Query New Customers
        List<Object[]> customersData = switch (granularity) {
            case "HOUR" -> newCustomerAuditRepository.countNewCustomersByHour(start, end);
            case "DAY" -> newCustomerAuditRepository.countNewCustomersByDay(start, end);
            case "MONTH" -> newCustomerAuditRepository.countNewCustomersByMonth(start, end);
            default -> newCustomerAuditRepository.countNewCustomersByDay(start, end);
        };
        
        for (Object[] row : customersData) {
            if (row == null || row.length < 4) continue;
            StatisticsDetailResponse.StatisticsDataPoint point = getOrCreatePoint.apply(row);
            Long newCustomers = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            point.setNewCustomers(newCustomers);
        }
        
        // Query Products Sold
        List<Object[]> productsData = switch (granularity) {
            case "HOUR" -> productSaleAuditRepository.sumProductsSoldByHour(start, end);
            case "DAY" -> productSaleAuditRepository.sumProductsSoldByDay(start, end);
            case "MONTH" -> productSaleAuditRepository.sumProductsSoldByMonth(start, end);
            default -> productSaleAuditRepository.sumProductsSoldByDay(start, end);
        };
        
        for (Object[] row : productsData) {
            if (row == null || row.length < 4) continue;
            StatisticsDetailResponse.StatisticsDataPoint point = getOrCreatePoint.apply(row);
            Long productsSold = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            point.setProductsSold(productsSold);
        }
        
        return dataMap;
    }

    // Helper class để lưu thông tin time slot
    private static class TimeSlot {
        private final String key;
        private final String label;
        private final LocalDateTime start;
        private final LocalDateTime end;
        
        public TimeSlot(String key, String label, LocalDateTime start, LocalDateTime end) {
            this.key = key;
            this.label = label;
            this.start = start;
            this.end = end;
        }
        
        public String getKey() { return key; }
        public String getLabel() { return label; }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
    }
}


