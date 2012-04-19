package is.gussi.bukkit.plugin.sandkassinn.bans.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;

import is.gussi.bukkit.plugin.sandkassinn.Sandkassinn;
import is.gussi.bukkit.plugin.sandkassinn.bans.BanData;
import is.gussi.bukkit.plugin.sandkassinn.bans.Datasource;

public class DatasourceMySQL implements Datasource {

	public DatasourceMySQL() {
		try {
			this.db().prepareStatement(DatasourceMySQL.getSQL("sandkassinn_bans.sql")).execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean add(BanData data) {
		PreparedStatement ps = null;
		try {
			ps = this.db().prepareStatement("INSERT INTO `sandkassinn_bans` (`banned`, `type`, `reason`, `executor`, `date_executed`, `date_expire`) VALUES(?, ?, ?, ?, ?, ?)");
			ps.setString(1, data.banned);
			ps.setString(2, data.type.name());
			ps.setString(3, data.reason);
			ps.setString(4, data.executor);
			ps.setLong(5, data.date_executed);
			ps.setLong(6, data.date_expire);
			return ps.executeUpdate() != 0;
		} catch (SQLException e) {
			Sandkassinn.log.severe("MySQL error occurred in ban module.");
			e.printStackTrace();
		} finally {
			try {
				if(ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public BanData check(String player) {
		PreparedStatement ps = null;
		try {
			ps = this.db().prepareStatement("SELECT `banned`, `type`, `reason`, `executor`, `date_executed`, `date_expire` FROM `sandkassinn_bans` WHERE `banned` = ? ORDER BY `id` DESC LIMIT 1");
			ps.setString(1, player);
			ResultSet ret = ps.executeQuery();
			if (ret.next()) {
				BanData data = new BanData();
				data.banned = ret.getString(1);
				data.type = BanData.Type.valueOf(ret.getString(2).toUpperCase());
				data.reason = ret.getString(3);
				data.executor = ret.getString(4);
				data.date_executed = ret.getLong(5);
				data.date_expire = ret.getLong(6);
				return data;
			}
		} catch (SQLException e) {
			Sandkassinn.log.severe("MySQL error occurred in ban module.");
			e.printStackTrace();
		} finally {
			try {
				if(ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private Connection db() {
		FileConfiguration fc = Sandkassinn.plugin.getConfig();
		try {
			return DriverManager.getConnection("jdbc:mysql://" + fc.getString("sandkassinn.datasource.mysql.host") + ":" + Integer.toString(fc.getInt("sandkassinn.datasource.mysql.port")) + "/" + fc.getString("sandkassinn.datasource.mysql.database") + "?autoReconnect=true&user=" + fc.getString("sandkassinn.datasource.mysql.username") + "&password=" + fc.getString("sandkassinn.datasource.mysql.password"));
		} catch (SQLException ex) {
			Sandkassinn.log.severe("MySQL connection error.");
			ex.printStackTrace();
		}
		return null;
	}

	private static String getSQL(String table) {
		InputStream is = null;
		BufferedReader br = null;
		String line;
		StringBuilder sb = new StringBuilder();

		try { 
			is = Sandkassinn.class.getResourceAsStream("/sql/" + table);
			br = new BufferedReader(new InputStreamReader(is));
			while (null != (line = br.readLine())) {
				sb.append(line);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) br.close();
				if (is != null) is.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}