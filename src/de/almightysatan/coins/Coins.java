package de.almightysatan.coins;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Coins {

	static final String PREFIX = "§7[§eCoins§7] ";

	static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
	private static Mysql sql;
	private static Map<UUID, Integer> balance = new HashMap<>();

	static void init(String url, String user, String password) {
		try {
			sql = new Mysql(url, user, password);
		} catch(SQLException e) {
			throw new Error("Unable to connect to database", e);
		}
	}

	static void loadPlayer(UUID uuid) {
		try {
			balance.put(uuid, sql.getCoins(uuid));
		} catch(SQLException e) {
			throw new Error("Unable to load coins for uuid " + uuid, e);
		}
	}

	public static void getOfflineCoins(UUID uuid, Consumer<Integer> callback) {
		if(balance.containsKey(uuid))
			callback.accept(balance.get(uuid));
		else
			EXECUTOR.execute(() -> {
				try {
					callback.accept(sql.getCoins(uuid));
				} catch(SQLException e) {
					throw new Error("Unable to load coins for uuid " + uuid, e);
				}
			});
	}

	public static int getCoins(UUID uuid) {
		return balance.get(uuid);
	}

	public static void setCoins(UUID uuid, int amount) {
		if(balance.containsKey(uuid))
			balance.put(uuid, amount);

		EXECUTOR.execute(() -> {
			try {
				sql.setCoins(uuid, amount);
			} catch(SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void addCoins(UUID uuid, int amount) {
		if(balance.containsKey(uuid))
			balance.put(uuid, balance.get(uuid) + amount);

		EXECUTOR.execute(() -> {
			try {
				sql.addCoins(uuid, amount);
			} catch(SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void removeCoins(UUID uuid, int amount) {
		addCoins(uuid, -amount);
	}
}
