package jpabook.japshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;


    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") //맵핑된거일 것 뿐이다.
    private List<Order> orders = new ArrayList<>();


}
