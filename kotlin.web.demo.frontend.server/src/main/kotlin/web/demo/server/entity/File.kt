package web.demo.server.entity

import javax.persistence.*

/**
 * @author Alexander Prendota on 2/8/18 JetBrains.
 */
@Entity
@Table(name = "FILES")
open class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    var id: Int = 0

    @Column(name = "PUBLIC_ID", unique = true, nullable = false)
    var publicId: String? = null

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID")
    var projectId: Project? = null

    @Column(name = "NAME", length = 100)
    var name: String? = null

    @Lob
    @Column(name = "CONTENT")
    var text: String? = null

}
/*
 * Legacy SQL
 */
//id         INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
//public_id  VARCHAR(45)  NOT NULL UNIQUE,
//project_id INT          NOT NULL,
//name       VARCHAR(100) NOT NULL,
//content    LONGTEXT,
//CONSTRAINT file_name UNIQUE (project_id, name),
//FOREIGN KEY (project_id) REFERENCES projects (id)
//ON DELETE CASCADE