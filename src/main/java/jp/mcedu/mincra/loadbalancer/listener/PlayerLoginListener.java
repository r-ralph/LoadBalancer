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

package jp.mcedu.mincra.loadbalancer.listener;

import jp.mcedu.mincra.loadbalancer.LoadBalancer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

public class PlayerLoginListener implements Listener {

    private LoadBalancer plugin;

    public PlayerLoginListener(LoadBalancer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        int count = plugin.getProxy().getOnlineCount() + 1;
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try (Jedis jedis = plugin.getPool().getResource()) {
                jedis.set(plugin.getConfig().getTableName(), String.valueOf(count));
                plugin.getLogger().info("Update count : " + count);
            }
        });
        plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.doLoadBalance(event.getPlayer()), 1, TimeUnit.SECONDS);
    }
}
