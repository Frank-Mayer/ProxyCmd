package io.rankmayer.proxycmd

import net.md_5.bungee.api.ProxyServer
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class Proxycmd : JavaPlugin(), CommandExecutor, TabCompleter {
    private var initialized = false

    private var _proxyServer: ProxyServer? = null

    private val proxyServer: ProxyServer
        get() {
            init()
            return _proxyServer!!
        }

    private fun init() {
        if (!initialized) {
            _proxyServer = ProxyServer.getInstance()
            initialized = true
            this.server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (command.name.equals("proxycmd", true) && args.size == 3) {
            try {
                when (args[0]) {
                    "send" -> {
                        val player = server.getPlayer(args[1])
                        if (player != null) {
                            sendPlayerToServer(player, args[2])
                        } else {
                            sender.sendMessage("${ChatColor.RED}Player not found: ${args[1]}")
                        }
                    }
                }
            } catch (e: Exception) {
                sender.sendMessage("${ChatColor.RED}Error: ${e.message}")
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (command.name.equals("proxycmd", true)) {
            try {
                return when (args.size) {
                    0 -> listOf("proxycmd")
                    1 -> listOf("send")
                    2 -> when (args[0]) {
                        "send" -> server.onlinePlayers.map { it.playerListName }
                        else -> null
                    }
                    3 -> when (args[0]) {
                        "send" -> try {
                            proxyServer.servers.map { it.key }
                        } catch (e: Exception) {
                            listOf(server.name)
                        }
                        else -> null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                sender.sendMessage("${ChatColor.RED}Error: ${e.message}")
                return null
            }
        }

        return null
    }

    private fun sendPlayerToServer(player: Player, server: String) {
        try {
            init()

            val b = ByteArrayOutputStream()
            val out = DataOutputStream(b)
            out.writeUTF("Connect")
            out.writeUTF(server)
            player.sendPluginMessage(this, "BungeeCord", b.toByteArray())
            b.close()
            out.close()
        } catch (e: Exception) {
            player.sendMessage("${ChatColor.RED}Error when trying to connect to $server: ${e.message}")
        }
    }
}
