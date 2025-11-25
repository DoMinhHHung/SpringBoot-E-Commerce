package iuh.fit.se.ecommerce.entity;

import iuh.fit.se.ecommerce.entity.enums.AuthProvider;
import iuh.fit.se.ecommerce.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phone;

    private String password;
    private String avatar;
    private String gender;
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private boolean enabled = false;

    @Builder.Default
    private boolean banned = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<Address> addresses = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}