package web.demo.server.entity

import web.demo.server.model.ProviderType
import javax.persistence.*

/**
 * @author Alexander Prendota on 2/6/18 JetBrains.
 */
@Entity
@Table(name = "USERS",
        uniqueConstraints = [UniqueConstraint(columnNames = ["ID", "PROVIDER"])])
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    var id: Int = 0

    @Column(name = "USERNAME")
    var username: String? = null

    @Column(name = "CLIENT_ID", unique = true, nullable = false)
    var clientId: String? = null

    @Column(name = "PROVIDER", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    var provider: ProviderType? = null

}
/*
 * Legacy SQL
 */
//id        INT                                    NOT NULL PRIMARY KEY AUTO_INCREMENT,
//client_id VARCHAR(45)                            NOT NULL,
//provider  ENUM ('google', 'twitter', 'facebook', 'github', 'jba') NOT NULL,
//username VARCHAR(100) NOT NULL             DEFAULT '',
//CONSTRAINT client_id UNIQUE (client_id, provider)