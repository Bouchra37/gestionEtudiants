package ma.projet.projetgestion.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ma.projet.projetgestion.R;
import ma.projet.projetgestion.beans.Filiere;

public class FiliereFragment extends Fragment {

    private EditText code, libelle;
    private Button bnAdd;
    private String insertUrl = "http://10.0.2.2:8080/api/v1/filieres";
    private String listUrl = "http://10.0.2.2:8080/api/v1/filieres";
    LinearLayout filiereListLayout;
    private String selectedFiliereId = null;

    private EditText editCode, editLibelle;

    private void fetchDataAndPopulateList() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                filiereListLayout.removeAllViews();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject filiereObject = response.getJSONObject(i);
                        String filiereId = filiereObject.getString("id");
                        String code = filiereObject.getString("code");
                        String libelle = filiereObject.getString("libelle");
                        View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_filiere, null);

                        TextView codeTextView = listItemView.findViewById(R.id.CodeTextView);
                        TextView libelleTextView = listItemView.findViewById(R.id.LibelleTextView);

                        codeTextView.setText(code);
                        libelleTextView.setText(libelle);

                        listItemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showOptionsDialog(filiereId);
                            }
                        });

                        filiereListLayout.addView(listItemView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filiere, container, false);

        code = view.findViewById(R.id.code);
        libelle = view.findViewById(R.id.libelle);
        bnAdd = view.findViewById(R.id.bnAdd);
        filiereListLayout = view.findViewById(R.id.filiereListLayout);

        bnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codeText = code.getText().toString();
                String libelleText = libelle.getText().toString();

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("code", codeText);
                    jsonBody.put("libelle", libelleText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, insertUrl, jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                builder.setMessage("Ajout avec succès")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                code.setText("");
                                                libelle.setText("");
                                                fetchDataAndPopulateList();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

                requestQueue.add(request);
            }
        });

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        fetchDataAndPopulateList();

        return view;
    }
    private void performSearch(String query) {
        String searchUrl = listUrl + "?search=" + query;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                updateFiliereList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }

    private void updateFiliereList(JSONArray searchResults) {
        filiereListLayout.removeAllViews();

        for (int i = 0; i < searchResults.length(); i++) {
            try {
                JSONObject filiereObject = searchResults.getJSONObject(i);
                String filiereId = filiereObject.getString("id");
                String code = filiereObject.getString("code");
                String libelle = filiereObject.getString("libelle");
                View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_filiere, null);

                TextView codeTextView = listItemView.findViewById(R.id.CodeTextView);
                TextView libelleTextView = listItemView.findViewById(R.id.LibelleTextView);

                codeTextView.setText(code);
                libelleTextView.setText(libelle);

                listItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOptionsDialog(filiereId);
                    }
                });

                filiereListLayout.addView(listItemView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showOptionsDialog(String filiereId) {
        selectedFiliereId = filiereId;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setTitle("Séléctionner votre option: ");

        dialogBuilder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSimpleEditPopup(filiereId);
            }
        });

        dialogBuilder.setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog(filiereId);
            }
        });

        dialogBuilder.show();
    }

    private void showSimpleEditPopup(String filiereId) {
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(requireContext());

        View editView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_layout, null);
        editCode = editView.findViewById(R.id.editCode);
        editLibelle = editView.findViewById(R.id.editLibelle);

        fetchDataForFiliere(filiereId);

        editDialogBuilder.setView(editView);

        editDialogBuilder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedCode = editCode.getText().toString();
                String updatedLibelle = editLibelle.getText().toString();

                updateFiliere(filiereId, updatedCode, updatedLibelle);
            }
        });

        editDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        editDialogBuilder.show();
    }
    private void fetchDataForFiliere(String filiereId) {
        String filiereDataUrl = "http://10.0.2.2:8080/api/v1/filieres/" + filiereId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, filiereDataUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String code = response.getString("code");
                    String libelle = response.getString("libelle");

                    editCode.setText(code);
                    editLibelle.setText(libelle);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }


    private void updateFiliere(String filiereId, String updatedCode, String updatedLibelle) {
        String updateUrl = "http://10.0.2.2:8080/api/v1/filieres/" + filiereId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("code", updatedCode);
            jsonBody.put("libelle", updatedLibelle);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, updateUrl, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showSuccessMessage("Modification réussie");
                fetchDataAndPopulateList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }

    private void showConfirmationDialog(String filiereId) {


        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(requireContext());
        confirmDialogBuilder.setMessage("Voulez-vous vraiment supprimer cet élément ?");
        confirmDialogBuilder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFiliere(filiereId);
            }
        });

        confirmDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        confirmDialogBuilder.show();
    }


    private void deleteFiliere(String filiereId) {
        String deleteUrl = "http://10.0.2.2:8080/api/v1/filieres/" + filiereId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showSuccessMessage("Suppression réussie");
                fetchDataAndPopulateList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(deleteRequest);
    }

    private void showSuccessMessage(String message) {
        AlertDialog.Builder successDialogBuilder = new AlertDialog.Builder(requireContext());
        successDialogBuilder.setMessage(message);
        successDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fetchDataAndPopulateList();
                dialog.dismiss();
            }
        });

        AlertDialog successDialog = successDialogBuilder.create();
        successDialog.show();
    }
}
