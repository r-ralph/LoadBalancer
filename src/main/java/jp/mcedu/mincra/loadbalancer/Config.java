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


import net.md_5.bungee.config.Configuration;

public class Config {

    private String address;

    private int port;

    private String password;

    private String tableName;

    private Config() {

    }

    public static Config load(Configuration config) {
        Config c = new Config();
        Configuration redis = config.getSection("redis");
        c.address = redis.getString("address");
        c.port = redis.getInt("port");
        c.password = redis.getString("password", null);
        c.tableName = redis.getString("table");
        return c;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getTableName() {
        return tableName;
    }
}
