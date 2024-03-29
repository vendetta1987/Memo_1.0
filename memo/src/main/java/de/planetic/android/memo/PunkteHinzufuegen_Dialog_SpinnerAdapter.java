package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Eigene Implementierung eines {@link ArrayAdapter} zur Nutzung mit
 * {@link Spinner}. Um Texte und Bilder im {@link Spinner} anzeigen zu können,
 * reicht der normale {@link ArrayAdapter} nicht aus. Zusätzlich wird eine
 * eigene Layout-Datei benötigt.
 * 
 */
public class PunkteHinzufuegen_Dialog_SpinnerAdapter extends
		ArrayAdapter<HashMap<String, Object>> {

	private PunkteZeigen_Tab punktezeigen_tab_context;
	private ArrayList<HashMap<String, Object>> arraylist_daten;

	/**
	 * Konstruktor nimmt {@link Context} und {@link ArrayList} entgegen.
	 * {@link Context} wird für den Zugriff auf den {@link LayoutInflater} der
	 * {@link Activity} benötigt. {@link ArrayList} enthält die anzuzeigenden
	 * Daten.
	 * 
	 * @param context
	 *            {@link Context} für den Zugriff auf den {@link LayoutInflater}
	 *            der {@link Activity}
	 * @param arraylist_daten
	 *            {@link ArrayList} mit {@link HashMap} und den Einträgen
	 *            {@code icon_name} als {@link String} und {@code icon_datei}
	 *            als {@link Drawable}
	 */
	public PunkteHinzufuegen_Dialog_SpinnerAdapter(Context context,
			ArrayList<HashMap<String, Object>> arraylist_daten) {
		super(
				context,
				R.layout.punktezeigen_tab_dialog_pkthinzufuegen_spinner_item_layout,
				arraylist_daten);

		punktezeigen_tab_context = (PunkteZeigen_Tab) ((MemoSingleton) context
				.getApplicationContext()).context_punktezeigen_tab;
		this.arraylist_daten = arraylist_daten;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	/**
	 * Erzeugt einen Eintrag im {@link Spinner} und liest die nötigen Daten aus
	 * der {@link ArrayList}
	 * 
	 * @param position
	 *            Position des Eintrages in der Liste
	 * @param convertView
	 *            {@link View} des gewählten Eintrages
	 * @param parent
	 *            {@link ViewGroup} zu der dieser Eintrag gehört
	 * @return {@link View} erzeugt nach den Vorgaben der Layout-Datei und
	 *         gefüllt mit den ausgelesenen Daten
	 */
	private View getCustomView(int position, View convertView, ViewGroup parent) {

		View view_eintrag = punktezeigen_tab_context
				.getLayoutInflater()
				.inflate(
						R.layout.punktezeigen_tab_dialog_pkthinzufuegen_spinner_item_layout,
						parent, false);

		((TextView) view_eintrag
				.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_spinner_item_layout_textview1))
				.setText(arraylist_daten.get(position).get("icon_name")
						.toString());

		((ImageView) view_eintrag
				.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_spinner_item_layout_imageview1))
				.setImageDrawable((Drawable) arraylist_daten.get(position).get(
						"icon_datei"));

		return view_eintrag;
	}
}