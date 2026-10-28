package ru.leonaugust.link_aggregator.postgres.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "user_communication")
public class UserCommunicationEntity {

  @Id
  private UUID id;

  @Column(name = "email")
  private String email;

  @Column(name = "telegram")
  private String telegram;

  @Column(name = "preferred_channel")
  @Enumerated(EnumType.STRING)
  private CommunicationChannelDbEnum preferredChannel;

  @Column(name = "language")
  @Enumerated(EnumType.STRING)
  private UserLanguageDbEnum language;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  private UserEntity user;

}