package m3.users.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Long id;

    @Column(name = "socnetuserid")
    private long socNetUserId;

    @Column(name = "socnettypeid")
    private int socNetTypeId;

    @Column(name = "create_tm")
    private long create_tm;

    @Column(name = "login_tm")
    private long login_tm;

    @Column(name = "logout_tm")
    private long logout_tm;

    @Column(name = "nextpointid")
    private long nextPointId;

    @Column(name = "fullrecoverytime")
    private long fullRecoveryTime;
}
