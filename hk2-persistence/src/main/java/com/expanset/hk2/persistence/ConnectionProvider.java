package com.expanset.hk2.persistence;

import java.sql.Connection;

/**
 * Receiving JDBC of connection from the ORM engine.
 */
public interface ConnectionProvider extends AutoCloseable {

	/**
	 * Returns the JDBC connection with a DB. It isn't necessary to close connection. 
	 * It is necessary to call close() in {@link ConnectionProvider}.
	 * @return JDBC connection with a DB.
	 */
	Connection provide();
}
