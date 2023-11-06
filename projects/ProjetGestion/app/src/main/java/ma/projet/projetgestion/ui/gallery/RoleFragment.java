package ma.projet.projetgestion.ui.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import ma.projet.projetgestion.R;

public class RoleFragment extends Fragment {

    private EditText name;
    private Button bnAdd;
    private String insertUrl = "http://10.0.2.2:8080/api/v1/roles";
    private String listUrl = "http://10.0.2.2:8080/api/v1/roles";
    LinearLayout roleListLayout;

    private String selectedRoleId = null;
    private EditText editName;

    private void fetchDataAndPopulateList() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                roleListLayout.removeAllViews();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject roleObject = response.getJSONObject(i);
                        String roleId = roleObject.getString("id");
                        String name = roleObject.getString("name");
                        View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_role, null);

                        TextView nameTextView = listItemView.findViewById(R.id.NameTextView);

                        nameTextView.setText(name);

                        listItemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showOptionsDialog(roleId);
                            }
                        });

                        roleListLayout.addView(listItemView);
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
        View view = inflater.inflate(R.layout.fragment_role, container, false);

        name = view.findViewById(R.id.name);
        bnAdd = view.findViewById(R.id.bnAdd);
        roleListLayout = view.findViewById(R.id.roleListLayout);

        bnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameText = name.getText().toString();

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("name", nameText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                        insertUrl, jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("resultat", response + "");

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                builder.setMessage("Ajout avec succès")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                name.setText("");
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

        fetchDataAndPopulateList();

        return view;
    }

    private void showOptionsDialog(String roleId) {
        selectedRoleId = roleId;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setTitle("Options");

        dialogBuilder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSimpleEditPopup(roleId);
            }
        });

        dialogBuilder.setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog(roleId);
            }
        });

        dialogBuilder.show();
    }

    private void showSimpleEditPopup(String roleId) {
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(requireContext());

        View editView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_layout_role, null);
        editName = editView.findViewById(R.id.editName);

        fetchDataForRole(roleId);

        editDialogBuilder.setView(editView);

        editDialogBuilder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedName = editName.getText().toString();

                updateRole(roleId, updatedName);
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

    private void fetchDataForRole(String roleId) {
        String roleDataUrl = "http://10.0.2.2:8080/api/v1/roles/" + roleId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, roleDataUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");

                    editName.setText(name);
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

    private void updateRole(String roleId, String updatedName) {
        String updateUrl = "http://10.0.2.2:8080/api/v1/roles/" + roleId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", updatedName);
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

    private void showConfirmationDialog(String roleId) {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(requireContext());
        confirmDialogBuilder.setMessage("Voulez-vous vraiment supprimer cet élément ?");
        confirmDialogBuilder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRole(roleId);
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

    private void deleteRole(String roleId) {
        String deleteUrl = "http://10.0.2.2:8080/api/v1/roles/" + roleId;

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
