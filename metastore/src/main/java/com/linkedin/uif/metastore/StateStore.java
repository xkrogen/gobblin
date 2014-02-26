package com.linkedin.uif.metastore;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.linkedin.uif.configuration.State;

/**
 * An interface for stores that persist {@link State}s.
 *
 * <p>
 *     Each such store consists of zero or more tables, and each table
 *     stores zero or more {@link State}s keyed on the state IDs (see
 *     {@link State#getId()).
 * </p>
 *
 * @author ynli
 */
public interface StateStore {

    /**
     * Create a new store.
     *
     * <p>
     *     A store that does not exist will be created when any put
     *     method is called against it.
     * </p>
     *
     * @param storeName store name
     * @return if the store is successfully created
     * @throws IOException
     */
    public boolean create(String storeName) throws IOException;

    /**
     * Create a new table in a store.
     *
     * <p>
     *     A table that does not exist will be created when any put
     *     method is called against it.
     * </p>
     *
     * @param storeName store name
     * @param tableName table name
     * @return if the table is succcessfully created
     * @throws IOException
     */
    public boolean create(String storeName, String tableName) throws IOException;

    /**
     * Put a {@link State} into a table.
     *
     * <p>
     *     Calling this method against a store or a table that
     *     does not exist will cause it to be created.
     * </p>
     *
     * @param storeName store name
     * @param tableName table name
     * @param state {@link State} to be put into the table
     * @throws IOException
     */
    public void put(String storeName, String tableName, State state)
            throws IOException;

    /**
     * Put a collection of {@link State}s into a table.
     *
     * <p>
     *     Calling this method against a store or a table that
     *     does not exist will cause it to be created.
     * </p>
     *
     * @param storeName store name
     * @param tableName table name
     * @param states collection of {@link State}s to be put into the table
     * @throws IOException
     */
    public void putAll(String storeName, String tableName, Collection<? extends State> states)
            throws IOException;

    /**
     * Get a {@link State} with a given state ID from a table.
     *
     * @param storeName store name
     * @param tableName table name
     * @param stateId state ID
     * @return {@link State} with the given state ID or <em>null</em>
     *         if the state with the given state ID does not exist
     * @throws IOException
     */
    public State get(String storeName, String tableName, String stateId)
            throws IOException;

    /**
     * Get all {@link State}s from a table.
     *
     * @param storeName store name
     * @param tableName table name
     * @return (possibly empty) list of {@link State}s from the given table
     * @throws IOException
     */
    public List<? extends State> getAll(String storeName, String tableName)
            throws IOException;

    /**
     * Get all {@link State}s from a store.
     *
     * @param storeName store name
     * @return (possibly empty) list of {@link State}s from the given store
     * @throws IOException
     */
    public List<? extends State> getAll(String storeName) throws IOException;
}