package jp.mcedu.mincra.loadbalancer;

import jp.mcedu.mincra.loadbalancer.listener.PlayerLoginListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LoadBalancer extends Plugin {

    private Config config;
    private JedisPool pool;

    @Override
    public void onEnable() {
        // Config
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Redis
        initRedis();

        // Event Listener
        getProxy().getPluginManager().registerListener(this, new PlayerLoginListener(this));

        getLogger().info("Enabled plugin successfully.");
    }

    private void loadConfig() throws IOException {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            Files.copy(getResourceAsStream("config.yml"), file.toPath());
        }

        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class)
                .load(new File(getDataFolder(), "config.yml"));
        config = Config.load(configuration);
    }

    private void initRedis() {
        pool = new JedisPool(new JedisPoolConfig(), config.getAddress(), config.getPort(),
                Protocol.DEFAULT_TIMEOUT, config.getPassword(), Protocol.DEFAULT_DATABASE);
    }


    public Config getConfig() {
        return config;
    }

    public JedisPool getPool() {
        return pool;
    }
}
