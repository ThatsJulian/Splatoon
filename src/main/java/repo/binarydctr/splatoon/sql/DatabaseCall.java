package repo.binarydctr.splatoon.sql;

/**
 * ******************************************************************
 * Copyright BinaryDctr (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of BinaryDctr. Distribution, reproduction, taking snippets, or
 * claiming any contents as your will break the terms of the license, and void any
 * agreements with you, the third party.
 * ******************************************************************
 **/
public abstract class DatabaseCall<PluginType extends Database> {

    protected PluginType plugin_;

    public DatabaseCall(PluginType plugin) {
        this.plugin_ = plugin;
    }

}
