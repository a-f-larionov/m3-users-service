package m3.users.entities;

import jakarta.persistence.*;
import lombok.*;
import m3.users.enums.SocNetType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "socnetuserid")
    private Long socNetUserId;

    @Column(name = "socnettypeid")
    private Long socNetTypeId;

    @Column(name = "create_tm")
    private Long createTm;

    @Column(name = "login_tm")
    private Long loginTm;

    @Column(name = "logout_tm")
    private Long logoutTm;

    @Column(name = "nextpointid")
    private Long nextPointId;

    @Column(name = "fullrecoverytime")
    private Long fullRecoveryTime;
}
