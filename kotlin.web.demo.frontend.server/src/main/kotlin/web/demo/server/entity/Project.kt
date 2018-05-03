package web.demo.server.entity

import web.demo.server.model.ConfType
import javax.persistence.*

/**
 * @author Alexander Prendota on 2/5/18 JetBrains.
 */
@Entity
@Table(name = "PROJECTS")
open class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    var id: Int = 0

    @Column(name = "NAME", length = 100)
    var name: String = ""

    @Column(name = "ARGS", length = 45)
    var args: String = ""

    @Column(name = "RUN_CONFIGURATION")
    @Enumerated(EnumType.STRING)
    var confType: ConfType = ConfType.java

    @Column(name = "ORIGIN")
    var originUrl: String? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "COMPILER_VERSION", length = 45)
    var compilerVersion: String? = null

    @Column(name = "READ_ONLY_FILES")
    var readOnlyFileNames: String = ""

    @ManyToOne
    @JoinColumn(name = "OWNER_ID")
    var ownerId: User? = null

    @Column(name = "PUBLIC_ID", nullable = false, unique = true)
    var publicId: String = ""

}

/*
 * Legacy SQL
 */
//id                INT                                    NOT NULL PRIMARY KEY AUTO_INCREMENT,
//public_id         VARCHAR(45)                            NOT NULL UNIQUE,
//owner_id          INT                                    NOT NULL,
//name              VARCHAR(100)                           NOT NULL             DEFAULT '',
//args              VARCHAR(45)                            NOT NULL             DEFAULT '',
//run_configuration ENUM ('java', 'js', 'canvas', 'junit') NOT NULL             DEFAULT 'java',
//origin            VARCHAR(100),
//read_only_files   TEXT                                   NOT NULL,
//type    ENUM('USER_PROJECT', 'KOANS_TASK', 'INCOMPLETE_KOANS_TASK', 'ADVENT_OF_CODE_PROJECT') NOT NULL DEFAULT 'USER_PROJECT',
//task_id INT   DEFAULT NULL, #NULL for user projects
//compiler_version VARCHAR (45) DEFAULT NULL,


//FOREIGN KEY (owner_id) REFERENCES users (id)
//ON DELETE CASCADE,
//FOREIGN KEY (task_id) REFERENCES koans_tasks (id)
//ON DELETE RESTRICT,
//CONSTRAINT `unique_name` UNIQUE (owner_id, name, type),
//CONSTRAINT `unique_solution` UNIQUE (owner_id, task_id)