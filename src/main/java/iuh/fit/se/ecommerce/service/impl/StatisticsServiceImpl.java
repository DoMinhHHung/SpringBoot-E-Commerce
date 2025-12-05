package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.response.StatisticsResponse;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import iuh.fit.se.ecommerce.repository.OrderItemRepository;
import iuh.fit.se.ecommerce.repository.OrderRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

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

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    public StatisticsResponse getStatistics(String period) {
        String normalized = normalizePeriod(period);
        LocalDateTime start = resolveStart(normalized);
        LocalDateTime end = resolveEnd(normalized, start);

        BigDecimal revenue = Objects.requireNonNullElse(
                orderRepository.sumTotalAmountByStatusAndCreatedAtBetween(SUCCESS_STATUSES, start, end),
                BigDecimal.ZERO
        );
        Long buyerCount = Objects.requireNonNullElse(
                orderRepository.countDistinctUserByStatusAndCreatedAtBetween(SUCCESS_STATUSES, start, end),
                0L
        );
        Long newCustomers = Objects.requireNonNullElse(
                userRepository.countByCreatedAtBetween(start, end),
                0L
        );
        Long productsSold = Objects.requireNonNullElse(
                orderItemRepository.sumQuantityByOrderStatusAndCreatedAtBetween(SUCCESS_STATUSES, start, end),
                0L
        );

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
}


