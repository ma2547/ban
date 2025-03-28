package me.deinname;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class AutoBanPlugin extends JavaPlugin {
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        dataFile = new File(getDataFolder(), "players.yml");
        if (!dataFile.exists()) {
            saveResource("players.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        startBanCheckTask();
    }

    @Override
    public void onDisable() {
        saveData();
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startBanCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L;

                for (String uuidString : dataConfig.getKeys(false)) {
                    long lastLogin = dataConfig.getLong(uuidString);
                    if (currentTime - lastLogin >= sevenDaysMillis) {
                        UUID uuid = UUID.fromString(uuidString);
                        Bukkit.getBanList(BanList.Type.NAME).addBan(Bukkit.getOfflinePlayer(uuid).getName(),
                                "Du warst mehr als 7 Tage inaktiv!", new Date(), "AutoBanSystem");
                        Bukkit.getLogger().info("Spieler " + Bukkit.getOfflinePlayer(uuid).getName() + " wurde gebannt!");
                    }
                }
            }
        }.runTaskTimer(this, 0L, 24 * 60 * 60 * 20L);
    }

    public void updateLastLogin(UUID uuid) {
        dataConfig.set(uuid.toString(), System.currentTimeMillis());
        saveData();
    }
}
