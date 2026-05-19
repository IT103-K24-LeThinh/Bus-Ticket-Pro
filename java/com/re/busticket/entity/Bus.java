package com.re.busticket.entity;

import com.re.busticket.entity.enums.BusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "bus_type")
    @Enumerated(EnumType.STRING)
    private BusType busType;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "is_active")
    private Boolean isActive;
}
