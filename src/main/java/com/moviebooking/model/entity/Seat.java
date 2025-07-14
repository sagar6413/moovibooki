package com.moviebooking.model.entity;

import com.moviebooking.model.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "screen_id", "seatRow", "seatNumber" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private Screen screen;

    private String seatRow; // Row label, e.g., "A", "B"
    private int seatNumber; // Seat number, e.g., 1, 2, 10

    @Enumerated(EnumType.STRING)
    private SeatCategory category; // Seat type: REGULAR, PREMIUM, VIP
}