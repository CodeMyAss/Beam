package me.aventium.projectbeam.collections;

import me.aventium.projectbeam.documents.Punishment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PunishmentRepository extends MongoRepository<Punishment, String> {

    public Punishment findActivePunishment(String player, Punishment.Type type);

    public List<Punishment> findActivePunishments(String player);

    public List<Punishment> findPunishments(String player);

}
