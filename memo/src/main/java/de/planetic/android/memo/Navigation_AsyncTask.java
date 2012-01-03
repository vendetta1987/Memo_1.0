package de.planetic.android.memo;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * {@link AsyncTask} für das Herunterladen, Auswerten und Verarbeiten von
 * Navigationsanfragen an Google. Nimmt einen Zielpunkt entgegen und ermittelt
 * den Startpunkt per GPS.
 * <p/>
 * {@code PROZENT_SCHRITTE} legt die Schrittweite der Fortschrittsdialoge fest <br/>
 * {@code MODUS_*} dienen der Unterscheidung ob Fortschrittsdialoge erstellt
 * oder aktualisiert werden sollen<br/>
 * {@code DIALOGTYP_*} unterscheiden die Fortschrittsdialoge
 */
public class Navigation_AsyncTask extends
		AsyncTask<GeoPunkt, Integer, HashMap<String, String>> {

	private Context context_con;
	private MemoSingleton memosingleton_anwendung;
	private ProgressDialog progress_spinner, progress_horizontal;
	private String string_urheberrecht;
	private static final int PROZENT_SCHRITTE = 10;
	private static final int MODUS_EINRICHTEN = 0;
	private static final int MODUS_AKTUALISIEREN = 1;
	private static final int DIALOGTYP_SPINNER = 2;
	private static final int DIALOGTYP_HORIZONTAL = 3;

	/**
	 * Konstruktor zur Bereitstellung eines {@link Context} und Zugriff auf
	 * {@link MemoSingleton}
	 * 
	 * @param con
	 *            {@link Context} für {@link Toast} und {@link Intent}
	 * @see MemoSingleton
	 */
	public Navigation_AsyncTask(Context con) {

		context_con = con;
		memosingleton_anwendung = (MemoSingleton) context_con
				.getApplicationContext();
	}

	/**
	 * Erzeugt die Fortschrittsdialoge und wechselt auf die Kartenansicht.
	 * 
	 * @see PunkteZeigen_Tab
	 */
	@Override
	protected void onPreExecute() {

		erzeugeDialoge();

		if (memosingleton_anwendung.context_punktezeigen_tab.tabhost
				.getCurrentTab() == PunkteZeigen_Tab.TAB_LISTE) {
			memosingleton_anwendung.context_punktezeigen_tab.tabhost
					.setCurrentTab(PunkteZeigen_Tab.TAB_KARTE);
		}
	}

	/**
	 * Erzeugt die Fortschrittdialoge für den Start und nach Drehung des
	 * Gerätes.
	 */
	private void erzeugeDialoge() {

		if (progress_spinner != null) {

			progress_spinner.dismiss();
		}
		if (progress_horizontal != null) {

			progress_horizontal.dismiss();
		}

		progress_spinner = new ProgressDialog(context_con);
		progress_horizontal = new ProgressDialog(context_con);

		progress_spinner
				.setTitle(R.string.punktezeigen_tab_dialog_text_navigiere);
		progress_spinner.setCancelable(false);
		progress_spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		progress_horizontal
				.setTitle(R.string.punktezeigen_tab_dialog_text_navigiere);
		progress_horizontal.setCancelable(false);
		progress_horizontal.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}

	/**
	 * {@code private int berechneProzentSchritte(int int_summe)}
	 * <p/>
	 * Berechnet die Anzahl von Elementen die einem bestimmten Prozentsatz der
	 * Gesamtmenge entsprechen.
	 * 
	 * @param int_summe
	 *            Summe aller Elemente
	 * @return für {@code int_summe} < 100 stets 1<br/>
	 *         sonst die Menge der Elemente die {@code PROZENT_SCHRITTE}-Prozent
	 *         ensprechen
	 */
	private int berechneProzentSchritte(int int_summe) {

		if ((int_summe * PROZENT_SCHRITTE) < 100) {

			return 1;
		} else {

			return ((int_summe * PROZENT_SCHRITTE) / 100);
		}
	}

	/**
	 * Aktualisiert die Fortschrittsdialoge. Aufgerufen durch
	 * {@code publishProgress(...)}.
	 * 
	 * @param int_aktualisierung
	 *            {@link Integer}-Array mit Steuerbefehlen und einzutragenden
	 *            Werten
	 */
	@Override
	protected void onProgressUpdate(Integer... int_aktualisierung) {
		// int_aktualisierung[]
		// 0-> modus (einrichten/aktualisieren)
		// 1-> dialogtyp (spinner/horizontal)
		// //einrichten spinner: 2-> text
		// //aktualisieren spinner: 2-> text
		// //einrichten horizontal: 2-> text, 3-> summe
		// //aktualisieren horizontal: 2-> zaehler

		switch (int_aktualisierung[0]) {

		case MODUS_EINRICHTEN:
			switch (int_aktualisierung[1]) {

			case DIALOGTYP_SPINNER:
				if (memosingleton_anwendung.boolean_gedreht) {

					erzeugeDialoge();
					memosingleton_anwendung.boolean_gedreht = false;
				}

				if (progress_horizontal.isShowing()) {

					progress_horizontal.hide();
				}
				progress_spinner.setMessage(context_con.getResources()
						.getString(int_aktualisierung[2]));
				if (!progress_spinner.isShowing()) {

					progress_spinner.show();
				}
				break;
			case DIALOGTYP_HORIZONTAL:
				if (memosingleton_anwendung.boolean_gedreht) {

					erzeugeDialoge();
					memosingleton_anwendung.boolean_gedreht = false;
				}

				if (progress_spinner.isShowing()) {

					progress_spinner.hide();
				}
				progress_horizontal.setMessage(context_con.getResources()
						.getString(int_aktualisierung[2]));
				progress_horizontal.setMax(int_aktualisierung[3]);
				if (!progress_horizontal.isShowing()) {

					progress_horizontal.show();
				}
				break;
			}
			break;
		case MODUS_AKTUALISIEREN:
			switch (int_aktualisierung[1]) {

			case DIALOGTYP_SPINNER:
				progress_spinner.setMessage(context_con.getResources()
						.getString(int_aktualisierung[2]));
				break;
			case DIALOGTYP_HORIZONTAL:
				progress_horizontal.setProgress(int_aktualisierung[2]);
				break;
			}
			break;
		}
	}

	/**
	 * Führt die weitere Verarbeitung im Hintergrund aus und sendet Nachrichten
	 * an den Vordergrundthread, der die Darstellung der Fortschrittsdialoge
	 * übernimmt. Startet GPS-Listener für die Positionsbestimmung und ruft
	 * SAX-Parser zur Auswertung der Antwort von Google auf. Dekodiert die
	 * mitgelieferten Linienzüge und erzeugt zwei {@link Path} zur Darstellung
	 * in {@link ItemOverlay}.
	 * 
	 * @param geopunkt_ziel
	 *            ein {@link GeoPunkt} als Zielpunkt der Navigationsanfrage
	 * @see GPS_Verwaltung
	 * @see Navigation_SAXHandler
	 * @see PunkteZeigen_Tab_Karte
	 */
	@Override
	protected HashMap<String, String> doInBackground(GeoPunkt... geopunkt_ziel) {

		NetworkInfo networkinfo_internet = ((ConnectivityManager) context_con
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		InputStream inputstream_daten = null;
		SAXParser saxparser_parser = null;
		Navigation_SAXHandler navigationsaxhandler_handler = null;
		HashMap<String, String> hashmap_ergebnis;
		ArrayList<GeoPunkt> arraylist_fein_kodiert, arraylist_grob_kodiert;
		Intent intent_befehl;
		ItemOverlay itemoverlay_route;
		MapView mapview_karte;
		Point point_temp, point_or, point_ul;
		Path path_pfad_grob, path_pfad_fein;
		RectF rectf_vergleich;
		boolean boolean_startziel;
		long long_zeit = System.currentTimeMillis();
		int int_span_lat, int_span_lon, int_summe, int_zaehler, int_zoom, int_prozent_temp;
		String string_url;
		// "http://maps.google.com/maps/api/directions/xml?origin=53.633333, 11.416667&destination=53.766667, 12.566667&sensor=false&language=de"

		memosingleton_anwendung.arraylist_karte_overlays_temp.clear();

		// zoom auf max fuer hoch aufgelösten pfad
		mapview_karte = (MapView) memosingleton_anwendung.context_punktezeigen_tab
				.getLocalActivityManager().getCurrentActivity()
				.findViewById(R.id.punktezeigen_karte_layout_mapview);

		int_zoom = mapview_karte.getZoomLevel();
		mapview_karte.getController().setZoom(21);

		point_or = memosingleton_anwendung.projection_karte.toPixels(
				new GeoPoint(45000000, 45000000), null);
		point_ul = memosingleton_anwendung.projection_karte.toPixels(
				new GeoPoint(-45000000, -45000000), null);

		intent_befehl = new Intent(MemoSingleton.INTENT_STARTE_GPS);
		intent_befehl.putExtra(context_con.getPackageName() + "_"
				+ "int_listener", MemoSingleton.GPS_LISTENER_NAVIGATION);
		context_con.sendBroadcast(intent_befehl);

		intent_befehl = new Intent(MemoSingleton.INTENT_STARTE_TTS);
		context_con.sendBroadcast(intent_befehl);

		publishProgress(MODUS_EINRICHTEN, DIALOGTYP_SPINNER,
				R.string.punktehinzufuegen_service_notification_warte_auf_gps);

		for (int i = 0; i < 12; i++) {

			if (memosingleton_anwendung.gps_verwaltung.long_letzte_aktualisierung > long_zeit) {
				break;
			}

			// TODO fuer nutzung wieder einschalten
			// SystemClock.sleep(5000);
		}

		intent_befehl = new Intent(MemoSingleton.INTENT_STOPPE_GPS);
		intent_befehl.putExtra(context_con.getPackageName() + "_"
				+ "int_listener", MemoSingleton.GPS_LISTENER_NAVIGATION);

		// TODO fuer nutzung anpassen
		// GeoPunkt geopunkt_start = memosingleton_anwendung.gps_verwaltung
		// .aktuellePosition();
		GeoPunkt geopunkt_start = new GeoPunkt(53633333, 11416667);// schwerin

		context_con.sendBroadcast(intent_befehl);

		// geopunkt_ziel[0] = new GeoPunkt(53766667, 12566667);//teterow
		// geopunkt_ziel[0] = new GeoPunkt(41973799, 2466103);// spanien

		string_url = "http://maps.google.com/maps/api/directions/xml?origin="
				+ String.valueOf(geopunkt_start.getLatitudeE6() / 1e6) + ","
				+ String.valueOf(geopunkt_start.getLongitudeE6() / 1e6)
				+ "&destination="
				+ String.valueOf(geopunkt_ziel[0].getLatitudeE6() / 1e6) + ","
				+ String.valueOf(geopunkt_ziel[0].getLongitudeE6() / 1e6)
				+ "&sensor=true&language=de";
		// +"&waypoints=optimize:true|54.083333,12.133333"

		if ((networkinfo_internet != null)
				&& (networkinfo_internet.isAvailable())) {

			try {

				publishProgress(
						MODUS_EINRICHTEN,
						DIALOGTYP_SPINNER,
						R.string.navigation_asynctask_dialog_text_empfange_daten);

				// TODO timeout?

				// TODO fuer nutzung anpassen
				// inputstream_daten = new URL(string_url).openStream();
				inputstream_daten = new URL(string_url).openConnection(
						new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
								"proxy.planet-ic.de", 8118))).getInputStream();

				saxparser_parser = SAXParserFactory.newInstance()
						.newSAXParser();

				navigationsaxhandler_handler = new Navigation_SAXHandler();

				publishProgress(
						MODUS_AKTUALISIEREN,
						DIALOGTYP_SPINNER,
						R.string.navigation_asynctask_dialog_text_verarbeite_daten);

				saxparser_parser.parse(inputstream_daten,
						navigationsaxhandler_handler);
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		if (navigationsaxhandler_handler != null) {

			if (navigationsaxhandler_handler.string_status
					.equalsIgnoreCase("OK")) {

				// daten auslesen
				string_urheberrecht = navigationsaxhandler_handler.string_urheberrecht;

				memosingleton_anwendung.arraylist_karte_navigationsanweisungen = navigationsaxhandler_handler.arraylist_html_anweisungen;

				arraylist_grob_kodiert = dekodiere(navigationsaxhandler_handler.string_grob_kodiert);

				arraylist_fein_kodiert = new ArrayList<GeoPunkt>();

				int_summe = navigationsaxhandler_handler.arraylist_fein_kodiert
						.size();
				int_zaehler = 1;

				publishProgress(MODUS_EINRICHTEN, DIALOGTYP_HORIZONTAL,
						R.string.navigation_asynctask_dialog_text_dekodiere,
						int_summe);

				int_prozent_temp = berechneProzentSchritte(int_summe);

				for (String string_fein_kodiert : navigationsaxhandler_handler.arraylist_fein_kodiert) {

					if ((int_zaehler % int_prozent_temp == 0)) {

						publishProgress(MODUS_AKTUALISIEREN,
								DIALOGTYP_HORIZONTAL, int_zaehler);
					}
					int_zaehler++;

					arraylist_fein_kodiert
							.addAll(dekodiere(string_fein_kodiert));
				}

				// pfade erzeugen
				point_temp = new Point();
				path_pfad_grob = new Path();
				path_pfad_fein = new Path();
				boolean_startziel = true;

				rectf_vergleich = new RectF((float) point_ul.x,
						(float) point_or.y, (float) point_or.x,
						(float) point_ul.y);

				int_zaehler = 1;
				int_summe = arraylist_grob_kodiert.size();

				publishProgress(
						MODUS_EINRICHTEN,
						DIALOGTYP_HORIZONTAL,
						R.string.navigation_asynctask_dialog_text_erzeuge_pfade,
						int_summe);

				int_prozent_temp = berechneProzentSchritte(int_summe);

				for (GeoPunkt punkt : arraylist_grob_kodiert) {

					if (boolean_startziel) {

						memosingleton_anwendung.projection_karte.toPixels(
								punkt, point_temp);
						path_pfad_grob.moveTo(point_temp.x, point_temp.y);

						boolean_startziel = false;
					}

					memosingleton_anwendung.projection_karte.toPixels(punkt,
							point_temp);
					path_pfad_grob.lineTo(point_temp.x, point_temp.y);
					path_pfad_grob.moveTo(point_temp.x, point_temp.y);

					if ((int_zaehler % int_prozent_temp) == 0) {

						publishProgress(MODUS_AKTUALISIEREN,
								DIALOGTYP_HORIZONTAL, int_zaehler);
					}
					int_zaehler++;
				}

				boolean_startziel = true;

				int_zaehler = 1;
				int_summe = arraylist_fein_kodiert.size();

				publishProgress(
						MODUS_EINRICHTEN,
						DIALOGTYP_HORIZONTAL,
						R.string.navigation_asynctask_dialog_text_erzeuge_pfade,
						int_summe);

				int_prozent_temp = berechneProzentSchritte(int_summe);

				for (GeoPunkt punkt : arraylist_fein_kodiert) {

					if (boolean_startziel) {

						memosingleton_anwendung.projection_karte.toPixels(
								punkt, point_temp);
						path_pfad_fein.moveTo(point_temp.x, point_temp.y);

						boolean_startziel = false;
					}

					memosingleton_anwendung.projection_karte.toPixels(punkt,
							point_temp);
					path_pfad_fein.lineTo(point_temp.x, point_temp.y);
					path_pfad_fein.moveTo(point_temp.x, point_temp.y);

					if ((int_zaehler % int_prozent_temp) == 0) {

						publishProgress(MODUS_AKTUALISIEREN,
								DIALOGTYP_HORIZONTAL, int_zaehler);
					}
					int_zaehler++;
				}

				// overlay mit pfaden erzeugen
				itemoverlay_route = new ItemOverlay(context_con.getResources()
						.getDrawable(R.drawable.dot), context_con,
						path_pfad_grob, path_pfad_fein, rectf_vergleich);

				itemoverlay_route.addOverlay(new OverlayItem(geopunkt_start,
						"Start", ""));
				itemoverlay_route.addOverlay(new OverlayItem(geopunkt_ziel[0],
						"Ziel", ""));

				itemoverlay_route.initialisieren();

				memosingleton_anwendung.arraylist_karte_overlays_temp
						.add(itemoverlay_route);
			}

			// gesamten pfad in karte anzeigen
			int_span_lat = geopunkt_start.getLatitudeE6()
					- geopunkt_ziel[0].getLatitudeE6();
			int_span_lon = geopunkt_start.getLongitudeE6()
					- geopunkt_ziel[0].getLongitudeE6();

			int_span_lat += int_span_lat / 40;
			int_span_lon += int_span_lon / 40;

			hashmap_ergebnis = new HashMap<String, String>();
			hashmap_ergebnis.put(
					"int_zentrum_lat",
					Integer.toString(geopunkt_start.getLatitudeE6()
							- (int_span_lat / 2)));
			hashmap_ergebnis.put(
					"int_zentrum_lon",
					Integer.toString(geopunkt_start.getLongitudeE6()
							- (int_span_lon / 2)));
			hashmap_ergebnis.put("int_span_lat",
					Integer.toString(Math.abs(int_span_lat)));
			hashmap_ergebnis.put("int_span_lon",
					Integer.toString(Math.abs(int_span_lon)));

			hashmap_ergebnis.put("string_status",
					navigationsaxhandler_handler.string_status);

			if (!navigationsaxhandler_handler.string_status
					.equalsIgnoreCase("OK")) {

				try {

					mapview_karte.getController().setZoom(int_zoom);
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			return hashmap_ergebnis;
		} else {

			return null;
		}
	}

	/**
	 * {@code private ArrayList<GeoPunkt> dekodiere(String string_kodiert)}
	 * <p/>
	 * Dekodiert die von Google kodierten Linienzüge in Geokoordinaten.
	 * Algorithmus siehe:<br/>
	 * www.facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/decode.html<br/>
	 * und<br/>
	 * http://www.code.google.com/intl/de/apis/maps/documentation/utilities/
	 * polylinealgorithm.html}<br/>
	 * 
	 * @param string_kodiert
	 * @return
	 */
	private ArrayList<GeoPunkt> dekodiere(String string_kodiert) {

		// http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/decode.html
		// http://code.google.com/intl/de/apis/maps/documentation/utilities/polylinealgorithm.html

		int int_laenge = string_kodiert.length(), int_index = 0, int_lat = 0, int_lon = 0, int_b, int_shift, int_ergebnis;
		ArrayList<GeoPunkt> arraylist_geopunkte = new ArrayList<GeoPunkt>();

		while (int_index < int_laenge) {

			int_shift = 0;
			int_ergebnis = 0;

			do {
				int_b = string_kodiert.charAt(int_index++) - 63;
				int_ergebnis |= (int_b & 0x1f) << int_shift;
				int_shift += 5;
			} while (int_b >= 0x20);
			int_lat += (((int_ergebnis & 1) != 0) ? ~(int_ergebnis >> 1)
					: (int_ergebnis >> 1));

			int_shift = 0;
			int_ergebnis = 0;

			do {
				int_b = string_kodiert.charAt(int_index++) - 63;
				int_ergebnis |= (int_b & 0x1f) << int_shift;
				int_shift += 5;
			} while (int_b >= 0x20);
			int_lon += (((int_ergebnis & 1) != 0) ? ~(int_ergebnis >> 1)
					: (int_ergebnis >> 1));

			if ((int_lat >= 80000000) || (int_lon >= 80000000)) {
				Log.d("memo_debug", "");
			}

			arraylist_geopunkte.add(new GeoPunkt(int_lat * 10, int_lon * 10));
		}

		return arraylist_geopunkte;
	}

	/**
	 * Wird nach der Beendigung des Hintergrundthreads aufgerufen,
	 * benachrichtigt {@link PunkteZeigen_Tab_Karte} und schließt die
	 * Fortschrittsdialoge.
	 * 
	 * @see PunkteZeigen_Tab_Karte
	 */
	@Override
	protected void onPostExecute(HashMap<String, String> hashmap_ergebnis) {

		Intent intent_befehl = new Intent(
				MemoSingleton.INTENT_HIERHIN_NAVIGIEREN);

		if ((hashmap_ergebnis != null)) {

			intent_befehl.putExtra(context_con.getPackageName() + "_"
					+ "string_status", hashmap_ergebnis.get("string_status"));
			intent_befehl.putExtra(context_con.getPackageName() + "_"
					+ "int_zentrum_lat",
					Integer.parseInt(hashmap_ergebnis.get("int_zentrum_lat")));
			intent_befehl.putExtra(context_con.getPackageName() + "_"
					+ "int_zentrum_lon",
					Integer.parseInt(hashmap_ergebnis.get("int_zentrum_lon")));
			intent_befehl.putExtra(context_con.getPackageName() + "_"
					+ "int_span_lat",
					Integer.parseInt(hashmap_ergebnis.get("int_span_lat")));
			intent_befehl.putExtra(context_con.getPackageName() + "_"
					+ "int_span_lon",
					Integer.parseInt(hashmap_ergebnis.get("int_span_lon")));

			if (hashmap_ergebnis.get("string_status").equalsIgnoreCase("OK")) {

				Toast.makeText(context_con, string_urheberrecht,
						Toast.LENGTH_LONG).show();
			}
		}

		context_con.sendBroadcast(intent_befehl);

		try {

			progress_spinner.dismiss();
			progress_horizontal.dismiss();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
