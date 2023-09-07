package com.cncmes.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public interface ResultSetHandler {
	public ArrayList<Object> doHandle(ResultSet rs) throws SQLException;
}
