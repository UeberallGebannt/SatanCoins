package de.almightysatan.coins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CoinsCommand {
	
	static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

	CoinsCommand(CoinsPlayer sender, String[] args) {
		if(args.length == 1)
			getUUID(args[0], (uuid) -> {
				if(uuid == null)
					sendInvalidPlayer(sender);
				else
					Coins.getOfflineCoins(uuid, (balance) -> sender.sendMessage(Coins.PREFIX + args[0] + " hat §e" + balance + " §7coins"));
			});
		else if(args.length == 0 || !sender.hasPermission("coins.admin"))
			sender.sendMessage(Coins.PREFIX + "Du hast §e" + Coins.getCoins(sender.getUUID()) + " §7coins");
		else if(args.length == 3) {
			final int amount;
			
			try {
				amount = Integer.parseInt(args[2]);
			}catch(NumberFormatException e) {
				sendUsage(sender);
				return;
			}
			
			getUUID(args[0], (uuid) -> {
				if(uuid == null)
					sendInvalidPlayer(sender);
				else
					switch(args[1].toLowerCase()) {
					case "add":
						Coins.addCoins(uuid, amount);
						sender.sendMessage(Coins.PREFIX + args[0] + " wurden §e" + amount + " §7 coins hinzugefügt");
						break;
					case "remove":
						Coins.removeCoins(uuid, amount);
						sender.sendMessage(Coins.PREFIX + args[0] + " wurden §e" + amount + " §7 coins hinzugefügt");
						break;
					case "set":
						Coins.setCoins(uuid, amount);
						sender.sendMessage(Coins.PREFIX + args[0] + " hat nun §e" + amount + " §7 coins");
						break;
					default:
						sendUsage(sender);
					}
			});
		}else
			sendUsage(sender);
	}
	
	private void getUUID(String playerName, Consumer<UUID> asyncCallback) {
		EXECUTOR.execute(() -> {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName).openStream()));

				String line = reader.readLine();

				reader.close();

				if(line != null) {
					String rawUUID = line.split("\"id\":\"")[1].substring(0, 32);
					asyncCallback.accept(UUID.fromString(rawUUID.substring(0, 8) + "-" + rawUUID.substring(8, 12) + "-" + rawUUID.substring(12, 16) + "-" + rawUUID.substring(16, 20) + "-" + rawUUID.substring(20)));
					return;
				}

			}catch(Throwable t) {}
			
			asyncCallback.accept(null);
		});
	}
	
	private void sendInvalidPlayer(CoinsPlayer sender) {
		sender.sendMessage(Coins.PREFIX + "Dieser Spieler existiert nicht");
	}
	
	private void sendUsage(CoinsPlayer sender) {
		sender.sendMessage(Coins.PREFIX + "Bitte nutze §e/coins <name> <add/remove/set> <amount> §7oder §e/coins <name>");
	}
}