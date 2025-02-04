package ru.elseff.crowdfoundingapi.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", nullable = false)
    String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    User author;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    List<Image> images;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<Comment> comments;
}
