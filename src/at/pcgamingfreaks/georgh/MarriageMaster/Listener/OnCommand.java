/*
 *   Copyright (C) 2014 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.georgh.MarriageMaster.Listener;

import java.util.Map;
import java.util.NavigableMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;

import at.pcgamingfreaks.georgh.MarriageMaster.Commands.*;
import at.pcgamingfreaks.georgh.MarriageMaster.Economy.HEconomy;
import at.pcgamingfreaks.georgh.MarriageMaster.*;

public class OnCommand implements CommandExecutor 
{
	private MarriageMaster marriageMaster;

	private MarryTp marryTp;
	private Priester priest;
	private Home home;
	
	public OnCommand(MarriageMaster marriagemaster) 
	{
		marriageMaster = marriagemaster;
		
		home = new Home(marriageMaster);
		marryTp = new MarryTp(marriageMaster);
		priest = new Priester(marriageMaster);
	}
	
	private void reload()
	{
		marriageMaster.config.Reload();
		marriageMaster.lang.Reload();
		marriageMaster.DB.Recache();
		if(marriageMaster.economy == null && marriageMaster.config.UseEconomy())
		{
			marriageMaster.economy = new HEconomy(marriageMaster);
		}
		else if(marriageMaster.economy != null && !marriageMaster.config.UseEconomy())
		{
			marriageMaster.economy = null;
		}
		
		if(marriageMaster.perms == null && marriageMaster.config.UsePermissions())
		{
			if(!marriageMaster.setupPermissions())
			{
				marriageMaster.config.SetPermissionsOff();
			}
		}
		else if(marriageMaster.perms != null && !marriageMaster.config.UsePermissions())
		{
			marriageMaster.perms = null;
		}
		PlayerInteractEvent.getHandlerList();
		HandlerList.unregisterAll(marriageMaster);
		marriageMaster.RegisterEvents();
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) 
	{
		Player player = null;
		if (sender instanceof Player) 
		{
			player = (Player) sender;
	    }
		else
		{
			if(args.length == 0)
			{
				ShowAvailableCmds(sender);
			}
			else if (args[0].equalsIgnoreCase("reload"))
	        {
				reload();
				sender.sendMessage(ChatColor.BLUE + "Reloaded");
	        }
			else if (args[0].equalsIgnoreCase("list"))
	        {
	        	NavigableMap<String, String> map = marriageMaster.DB.GetAllMarriedPlayers();
    			if(map.size() > 0)
    			{
    				sender.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListHL"));
    				
    				for(Map.Entry<String, String> item : map.entrySet())
    				{
    					sender.sendMessage(ChatColor.GREEN + item.getKey() + " + " + item.getValue());
    				}	
    			}	
    			else
    			{
    				sender.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoMarriedPlayers"));
    			}
	        }
			else if(args.length == 2 && args[0].equalsIgnoreCase(marriageMaster.config.GetPriestCMD()))
	        {
	        	priest.setPriest(args, sender);
	        }
	        else if (args.length == 2 && args[0].equalsIgnoreCase("divorce"))
	        {
	        	priest.Divorce(player, args);
	        }
	        else if (args.length == 2)
	        {
	        	priest.Marry(sender, args);
	        }
			else
			{
				ShowAvailableCmds(sender);
			}
			return true;
		}
		
		if(args.length == 0)
		{
			ShowAvailableCmds(player);
			return true;
		}
		else if (args[0].equalsIgnoreCase("reload"))
        {
			if(marriageMaster.config.CheckPerm(player, "marry.reload", false))
	    	{
				reload();
				player.sendMessage(ChatColor.BLUE + "Reloaded");
			}
			else
			{
				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
			}
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.list"))
    		{
    			NavigableMap<String, String> map = marriageMaster.DB.GetAllMarriedPlayers();

    			if(map.size() > 0)
    			{
    				player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListHL"));
    				
    				for(Map.Entry<String, String> item : map.entrySet())
    				{
    					player.sendMessage(ChatColor.GREEN + item.getKey() + " + " + item.getValue());
    				}	
    			}	
    			else
    			{
    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoMarriedPlayers"));
    			}
    		}
        	else
	    	{
	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
	    	}
        }
        else if (args[0].equalsIgnoreCase("pvpon")) 
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.pvpon"))
    	    	{
    	    		String partner = marriageMaster.DB.GetPartner(player.getName());
    	    		marriageMaster.DB.SetPvPEnabled(player.getName(), partner, true);
    	    		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PvPOn"));
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if (args[0].equalsIgnoreCase("pvpoff"))
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.pvpoff"))
    	    	{
    	    		String partner = marriageMaster.DB.GetPartner(player.getName());
    	    		marriageMaster.DB.SetPvPEnabled(player.getName(), partner, false);
    	    		player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.PvPOff"));
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if(args[0].equalsIgnoreCase("home"))
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.home"))
    	    	{
    	        	home.TP(player);
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if(args[0].equalsIgnoreCase("sethome"))
        {
        	if(marriageMaster.HasPartner(player.getName()))
        	{
    	    	if(marriageMaster.config.CheckPerm(player, "marry.home"))
    	    	{
    	        	home.SetHome(player);
    	    	}
    	    	else
    	    	{
    	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    	    	}
        	}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    		}
        }
        else if (args[0].equalsIgnoreCase("tp"))
        {
        	if(marriageMaster.HasPartner(player.getName()) && marriageMaster.config.CheckPerm(player, "marry.tp"))
    		{
    	        marryTp.TP(player);
        	}
    		else
    		{
    			if(marriageMaster.config.CheckPerm(player, "marry.tp"))
    			{
    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
    			}
    			else
    			{
    				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
    			}
    		}
        }
        else if(args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c"))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.chat"))
    		{
        		if(marriageMaster.HasPartner(player.getName()))
        		{
        			Player otP = marriageMaster.getServer().getPlayer(marriageMaster.DB.GetPartner(player.getName()));
        			if(otP != null && otP.isOnline())
        			{
        				String msg = player.getName() + " => " + otP.getName() + ":";
        				for(int i = 1; i < args.length; i++)
        				{
        					msg += " " + args[i];
        				}
        				otP.sendMessage(msg);
        				player.sendMessage(msg);
        				for (Player play : marriageMaster.pcl)
        				{
        					play.sendMessage(msg);
        				}
        			}
        			else
        			{
        				player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.PartnerOffline"));
        			}
        		}
        		else
        		{
        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotMarried"));
        		}
    		}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
        	}
        }
        else if (args[0].equalsIgnoreCase("listenchat"))
		{
        	if(marriageMaster.config.CheckPerm(player, "marry.listenchat"))
    		{
        		if(!marriageMaster.pcl.contains(player))
        		{
        			marriageMaster.pcl.add(player);
        			player.sendMessage(ChatColor.GREEN + marriageMaster.lang.Get("Ingame.ListeningStarted"));
        		}
        		else
        		{
        			marriageMaster.pcl.remove(player);
        			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.ListeningStoped"));
        		}
			}
	    	else
	    	{
	    		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
	    	}
		}
        else if(args.length == 2 && args[0].equalsIgnoreCase(marriageMaster.config.GetPriestCMD()))
        {
        	if(marriageMaster.config.CheckPerm(player, "marry.setpriest", false))
        	{
        		priest.setPriest(args, sender);
        	}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NoPermission"));
        	}
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("divorce"))
        {
        	if(marriageMaster.IsPriester(player))
        	{
        		priest.Divorce(player, args);
        	}
        	else
        	{
        		player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Description.NotAPriest"));
        	}
        }
        else if (args.length == 2)
        {
        	if(marriageMaster.IsPriester(player))
    		{
    			priest.Marry(player, args);
    		}
    		else
    		{
    			player.sendMessage(ChatColor.RED + marriageMaster.lang.Get("Ingame.NotAPriest"));
    		}
        }
        else if(args[0].equalsIgnoreCase("accept"))
        {
        	priest.AcceptMarriage(player);
        }
        else if(args[0].equalsIgnoreCase("deny"))
        {
        	for (Marry_Requests m : marriageMaster.mr)
    		{
        		if(m.p1 == player || m.p2 == player)
        		{
        			marriageMaster.mr.remove(m);
        			if(marriageMaster.config.UseConfirmation() && marriageMaster.config.UseConfirmationAutoDialog())
					{
        				player.chat(marriageMaster.lang.Get("Dialog.NoIDontWant"));
					}
        			m.priest.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerCalledOff"), player.getName()));
        			player.sendMessage(marriageMaster.lang.Get("Ingame.YouCalledOff"));
        			if(m.p1 == player)
        			{
        				m.p2.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerCalledOff"), player.getName()));
        			}
        			else
        			{
        				m.p1.sendMessage(String.format(marriageMaster.lang.Get("Ingame.PlayerCalledOff"), player.getName()));
        			}
        			return true;
        		}
    		}
        	player.sendMessage(marriageMaster.lang.Get("Priest.NoRequest"));
        }
        else
        {
        	ShowAvailableCmds(player);
        }
        return true;
	}
	
	public void ShowAvailableCmds(Player player)
	{
		player.sendMessage(ChatColor.YELLOW + "Marriage Master - " + marriageMaster.lang.Get("Description.Commands"));
		if(marriageMaster.config.CheckPerm(player, "marry.list"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry list" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListAll"));
		}
		if(marriageMaster.IsPriester(player))
		{
			player.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Marry"));
			player.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Divorce"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.tp"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry tp" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.TP"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.home"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry home" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.TPHome"));
			player.sendMessage(ChatColor.AQUA + "/marry sethome" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.SetHome"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.chat"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry chat" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Chat"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.pvpon"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpon" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.PvPOn"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.pvpoff"))
		{
			player.sendMessage(ChatColor.AQUA + "/marry pvpoff" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.PvPOff"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.setpriest", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry " + marriageMaster.config.GetPriestCMD() + " <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Priest"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.listenchat", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry listenchat" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListenChat"));
		}
		if(marriageMaster.config.CheckPerm(player, "marry.reload", false))
		{
			player.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Reload"));
		}
	}
	
	public void ShowAvailableCmds(CommandSender sender)
	{
		sender.sendMessage(ChatColor.YELLOW + "Marriage Master - " + marriageMaster.lang.Get("Description.Commands"));
		sender.sendMessage(ChatColor.AQUA + "/marry list" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.ListAll"));
		sender.sendMessage(ChatColor.AQUA + "/marry <Playername> <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Marry"));
		sender.sendMessage(ChatColor.AQUA + "/marry divorce <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Divorce"));
		sender.sendMessage(ChatColor.AQUA + "/marry priest <Playername>" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Priest"));
		sender.sendMessage(ChatColor.AQUA + "/marry reload" + ChatColor.WHITE + " - " + marriageMaster.lang.Get("Description.Reload"));
	}
}
