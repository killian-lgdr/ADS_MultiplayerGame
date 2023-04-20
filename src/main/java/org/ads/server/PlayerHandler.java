package org.ads.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHandler extends Thread {
    private final Socket playerSocket;
    private PrintWriter output;
    private Player player;
    public PlayerHandler(Socket playerSocket) {
        this.playerSocket = playerSocket;
    }

    public void run() {
        try {
            output = new PrintWriter(playerSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            String message;
            while ((message = input.readLine()) != null){
                String[] parts = message.split(" ");
                switch (parts[0]) {
                    case "JOIN" -> {
                        if (player == null) {
                            player = new Player(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5], parts[6]);
                            System.out.println(player.name + " connected.");
                        }
                        Main.broadcast("JOIN " + player.name + " " + player.worldX + " " + player.worldY + " " + player.speed + " " + player.direction + " " + player.skin, this);
                    }
                    case "UPDATE" -> {
                        player.update(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5], parts[6]);
                        sendMessage("UPDATE OK");
                        Main.broadcast("UPDATE " + player.name + " " + player.worldX + " " + player.worldY + " " + player.speed + " " + player.direction + " " + player.isMoving, this);
                    }
                    case "QUIT" -> {
                        System.out.println(player.name + " disconnected.");
                        Main.broadcast("QUIT " + player.name, this);
                        playerSocket.close();
                        return;
                    }
                    default -> {
                    }
                }
            }
            playerSocket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la communication avec le client " + player.name);
            e.printStackTrace();
        }
    }
    public void sendMessage(String message) {
        output.println(message);
    }
}
