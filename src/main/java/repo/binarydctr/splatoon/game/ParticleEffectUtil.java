package repo.binarydctr.splatoon.game;

import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/

public class ParticleEffectUtil {

    public void playAt(EnumParticle particle, Location location){
        for (Player player : location.getWorld ().getPlayers ()){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles (particle, true, (float)location.getX (), (float)location.getY (), (float)location.getZ (), 1, 1, 1, 0, 1, null);
            ((CraftPlayer) player).getHandle ().playerConnection.sendPacket (packet);
        }
    }

    public void playAt(EnumParticle particle, Location location, Player... players){
        for (Player player : players){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, (float)location.getX (), (float)location.getY (), (float)location.getZ (), 1, 1, 1, 0, 1, null);
            ((CraftPlayer) player).getHandle ().playerConnection.sendPacket (packet);
        }
    }

    public void playAt(EnumParticle particle, Location location, int offSetX, int offSetY, int offSetZ){
        for (Player player : location.getWorld ().getPlayers ()){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles (particle, true, (float)location.getX (), (float)location.getY (), (float)location.getZ (), offSetX, offSetY, offSetZ, 0, 1, null);
            ((CraftPlayer) player).getHandle ().playerConnection.sendPacket (packet);
        }
    }

    public void playAt(EnumParticle particle, Location location, int offSetX, int offSetY, int offSetZ, Player... players){
        for (Player player : players){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles (particle, true, (float)location.getX (), (float)location.getY (), (float)location.getZ (), offSetX, offSetY, offSetZ, 0, 1, null);
            ((CraftPlayer) player).getHandle ().playerConnection.sendPacket (packet);
        }
    }

    public void playAt(EnumParticle particle, Location location, int offSetX, int offSetY, int offSetZ, int particleType, int size){
        for (Player player : location.getWorld ().getPlayers ()){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles (particle, true, (float)location.getX (), (float)location.getY (), (float)location.getZ (), offSetX, offSetY, offSetZ, particleType, size, null);
            ((CraftPlayer) player).getHandle ().playerConnection.sendPacket (packet);
        }
    }

    public void playAt(EnumParticle particle, Location location, int offSetX, int offSetY, int offSetZ, int particleType, int size, Player... players){
        for (Player player : players){
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles (particle, true, (float)location.getX (), (float)location.getY (), (float)location.getZ (), offSetX, offSetY, offSetZ, particleType, size, null);
            ((CraftPlayer) player).getHandle ().playerConnection.sendPacket (packet);
        }
    }


}