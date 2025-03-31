package ru.elseff.crowdfoundingapi.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project {
    @Id
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", nullable = false)
    String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    User author;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = ProjectCategory.class)
    ProjectCategory category;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Image> images;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> comments;

    @Column(name = "target", nullable = false)
    Integer target;

    @Column(name = "collected", nullable = false)
    Integer collected;

    @Column(name = "closed", nullable = false)
    boolean closed;

    @Column(name = "closed_at")
    LocalDateTime closedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (target==null) {
            target = 500;
        }
        collected = 0;
        if (id==null){
            closed = false;
        }
    }
}
