/*
 * Copyright 2015 TENTO, Mincra, Ralph
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.mcedu.mincra.loadbalancer;

import jp.mcedu.mincra.loadbalancer.listener.PlayerLoginListener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
        getProxy().getScheduler().runAsync(this, () -> pool = new JedisPool(new JedisPoolConfig(), config.getAddress(), config.getPort(),
                Protocol.DEFAULT_TIMEOUT, config.getPassword(), Protocol.DEFAULT_DATABASE));
    }

    public void doLoadBalance(ProxiedPlayer player) {
        final int[] min = {Integer.MAX_VALUE};
        final ServerInfo[] minServer = new ServerInfo[1];
        getProxy().getServers().forEach((name, serverInfo) -> {
            if (name.equals("lobby")) {
                return;
            }
            int size = serverInfo.getPlayers().size();
            if (size < min[0]) {
                minServer[0] = serverInfo;
                min[0] = size;
            }
        });
        if (minServer[0] == null) {
            throw new RuntimeException("Can't find minimum players server");
        }
        player.connect(minServer[0], (success, throwable) -> {
            if (success) {
                getLogger().info(String.format("Send player \"%s\" to %s.", player.getName(), minServer[0].getName()));
            } else {
                getLogger().info(String.format("Can't send player \"%s\" to %s.", player.getName(), minServer[0].getName()));
                throwable.printStackTrace();
            }
        });
    }

    public Config getConfig() {
        return config;
    }

    public JedisPool getPool() {
        return pool;
    }
}
