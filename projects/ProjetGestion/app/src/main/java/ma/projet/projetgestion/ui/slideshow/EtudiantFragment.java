package ma.projet.projetgestion.ui.slideshow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;
import java.util.List;

import ma.projet.projetgestion.R;
import ma.projet.projetgestion.beans.Filiere;

public class EtudiantFragment extends Fragment {

    private EditText name, email, phone, username, password;
    private Button bnAdd;
    private String insertUrl = "http://10.0.2.2:8080/api/student";
    private String listUrl = "http://10.0.2.2:8080/api/student";
    private String listFiliereUrl = "http://10.0.2.2:8080/api/v1/filieres";
    private String listRoleUrl = "http://10.0.2.2:8080/api/v1/roles";

    LinearLayout etudiantListLayout;
    Spinner spinnerFiliere;
    Spinner spinnerRole;
    Spinner spinnerFiliere2;

    private List<String> rolesList = new ArrayList<>();
    private List<String> filieresList = new ArrayList();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_etudiant, container, false);

        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);

        bnAdd = view.findViewById(R.id.bnAdd);
        etudiantListLayout = view.findViewById(R.id.etudiantListLayout);
        spinnerFiliere = view.findViewById(R.id.spinnerFiliere);
        spinnerRole = view.findViewById(R.id.spinnerRole);

        spinnerFiliere2 = view.findViewById(R.id.spinnerFiliere2);

        fetchRoles();
        fetchFilieres();

        bnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStudentToDatabase();
            }
        });

        spinnerFiliere2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                searchByFiliere();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        fetchDataAndPopulateList();
        fetchFilieres();

        return view;
    }

    private void addStudentToDatabase() {
        String nameText = name.getText().toString();
        String emailText = email.getText().toString();
        String phoneText = phone.getText().toString();
        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();
        String selectedRole = spinnerRole.getSelectedItem().toString();
        String selectedFiliere = spinnerFiliere.getSelectedItem().toString();

        int filiereId = getFiliereIdByName(selectedFiliere);
        int roleId = getRoleIdByName(selectedRole);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", 0);
            JSONArray rolesArray = new JSONArray();
            JSONObject roleObject = new JSONObject();
            roleObject.put("id", roleId);
            rolesArray.put(roleObject);
            jsonBody.put("roles", rolesArray);
            jsonBody.put("username", usernameText);
            jsonBody.put("password", passwordText);
            jsonBody.put("name", nameText);
            jsonBody.put("phone", Integer.parseInt(phoneText));
            jsonBody.put("email", emailText);
            JSONObject filiereObject = new JSONObject();
            filiereObject.put("id", filiereId);
            jsonBody.put("filiere", filiereObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, insertUrl, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleAddStudentResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleErrorAddingStudent(error);
            }
        });

        requestQueue.add(request);
    }

    private void handleAddStudentResponse(JSONObject response) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Ajout avec succès")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                clearInputFields();
                                fetchDataAndPopulateList();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void handleErrorAddingStudent(VolleyError error) {
        Log.e("EtudiantFragment", "Error adding student: " + error.getMessage());
    }

    private void clearInputFields() {
        name.setText("");
        email.setText("");
        phone.setText("");
        username.setText("");
        password.setText("");
    }

    private void fetchRoles() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listRoleUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        rolesList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject roleObject = response.getJSONObject(i);
                                String roleName = roleObject.getString("name");
                                rolesList.add(roleName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        updateRoleSpinner();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error fetching role names: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private void fetchFilieres() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listFiliereUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        filieresList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject filiereObject = response.getJSONObject(i);
                                String filiereLibelle = filiereObject.getString("libelle");
                                filieresList.add(filiereLibelle);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        updateFiliereSpinner();
                        updateFiliereSpinner2();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error fetching filiere names: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private void updateRoleSpinner() {
        ArrayAdapter<String> roleSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, rolesList);
        roleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleSpinnerAdapter);
    }

    private void updateFiliereSpinner() {
        ArrayAdapter<String> filiereSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, filieresList);
        filiereSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiliere.setAdapter(filiereSpinnerAdapter);
    }

    private void updateFiliereSpinner2() {
        ArrayAdapter<String> filiereSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, filieresList);
        filiereSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiliere2.setAdapter(filiereSpinnerAdapter);
    }
    private void fetchDataAndPopulateList() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        etudiantListLayout.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject etudiantObject = response.getJSONObject(i);
                                String name = etudiantObject.getString("name");
                                String email = etudiantObject.getString("email");
                                int phone = etudiantObject.getInt("phone");
                                String username = etudiantObject.getString("username");
                                final String studentId = etudiantObject.getString("id");

                                JSONObject filiereObject = etudiantObject.getJSONObject("filiere");
                                String filiereName = filiereObject.getString("libelle");

                                final View listItemView = LayoutInflater.from(requireContext())
                                        .inflate(R.layout.list_item_etudiant, null);

                                final TextView nameTextView = listItemView.findViewById(R.id.NameTextView);
                                final TextView emailTextView = listItemView.findViewById(R.id.EmailTextView);
                                final TextView phoneTextView = listItemView.findViewById(R.id.PhoneTextView);
                                final TextView usernameTextView = listItemView.findViewById(R.id.UsernameTextView);
                                final TextView filiereTextView = listItemView.findViewById(R.id.FiliereTextView);

                                nameTextView.setText("Nom : " + name);
                                emailTextView.setText("Email : " + email);
                                phoneTextView.setText("Phone : " + String.valueOf(phone));
                                usernameTextView.setText("Username : " + username);
                                filiereTextView.setText("Filière : " + filiereName);

                                listItemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showOptionsDialog(studentId);
                                    }
                                });

                                etudiantListLayout.addView(listItemView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error fetching data: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private int getFiliereIdByName(String filiereName) {
        for (int i = 0; i < filieresList.size(); i++) {
            if (filieresList.get(i).equals(filiereName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private int getRoleIdByName(String roleName) {
        for (int i = 0; i < rolesList.size(); i++) {
            if (rolesList.get(i).equals(roleName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private void showOptionsDialog(final String studentId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setTitle("Options");

        dialogBuilder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showEditStudentDialog(studentId);
            }
        });

        dialogBuilder.setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog(studentId);
            }
        });

        dialogBuilder.show();
    }

    private void showEditStudentDialog(final String studentId) {

        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(requireContext());
        editDialogBuilder.setTitle("Modifier l'étudiant");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_layout_student, null);
        editDialogBuilder.setView(dialogView);

        final EditText editName = dialogView.findViewById(R.id.editName);
        final EditText editEmail = dialogView.findViewById(R.id.editEmail);
        final EditText editPhone = dialogView.findViewById(R.id.editPhone);
        final EditText editUsername = dialogView.findViewById(R.id.editUsername);

        fetchStudentData(studentId, new StudentDataCallback() {
            @Override
            public void onDataReceived(JSONObject studentData) {
                try {
                    editName.setText(studentData.getString("name"));
                    editEmail.setText(studentData.getString("email"));
                    editPhone.setText(String.valueOf(studentData.getInt("phone")));
                    editUsername.setText(studentData.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        editDialogBuilder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editName.getText().toString();
                String newEmail = editEmail.getText().toString();
                String newPhone = editPhone.getText().toString();
                String newUsername = editUsername.getText().toString();

                JSONObject updatedStudent = new JSONObject();
                try {
                    updatedStudent.put("name", newName);
                    updatedStudent.put("email", newEmail);
                    updatedStudent.put("phone", newPhone);
                    updatedStudent.put("username", newUsername);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateStudentData(studentId, updatedStudent);

                fetchDataAndPopulateList();
            }
        });


        editDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog editDialog = editDialogBuilder.create();
        editDialog.show();
    }
    public interface StudentDataCallback {
        void onDataReceived(JSONObject studentData);
    }

    private void showConfirmationDialog(final String studentId) {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(requireContext());
        confirmDialogBuilder.setMessage("Voulez-vous vraiment supprimer cet étudiant ?");
        confirmDialogBuilder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStudent(studentId);
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

    private void deleteStudent(final String studentId) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        String deleteUrl = "http://10.0.2.2:8080/api/student/" + studentId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(requireContext(), "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                        fetchDataAndPopulateList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error deleting student: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private void updateStudentData(String studentId, JSONObject updatedData) {
        String updateUrl = "http://10.0.2.2:8080/api/student/" + studentId;

        String selectedFiliere = spinnerFiliere.getSelectedItem().toString();

        int filiereId = getFiliereIdByName(selectedFiliere);

        try {
            updatedData.put("filiere_id", filiereId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, updateUrl, updatedData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        fetchDataAndPopulateList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error updating student data: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private void fetchStudentData(String studentId, final StudentDataCallback callback) {
        String apiUrl = "http://10.0.2.2:8080/api/student/" + studentId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onDataReceived(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error fetching student data: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }
    private void searchByFiliere() {
        String selectedFiliere = spinnerFiliere2.getSelectedItem().toString();
        int filiereId = getFiliereIdByName(selectedFiliere);
        String apiUrl = "http://10.0.2.2:8080/api/student/" + filiereId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleSearchByFiliereResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("EtudiantFragment", "Error searching by filiere: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }
    private void handleSearchByFiliereResponse(JSONObject response) {
        try {
            JSONArray studentArray = response.getJSONArray("students");

            etudiantListLayout.removeAllViews();

            for (int i = 0; i < studentArray.length(); i++) {
                JSONObject studentObject = studentArray.getJSONObject(i);

                String name = studentObject.getString("name");
                String email = studentObject.getString("email");
                int phone = studentObject.getInt("phone");
                String username = studentObject.getString("username");
                String filiereName = studentObject.getJSONObject("filiere").getString("libelle");


                View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_etudiant, null);

                TextView nameTextView = listItemView.findViewById(R.id.NameTextView);
                TextView emailTextView = listItemView.findViewById(R.id.EmailTextView);
                TextView phoneTextView = listItemView.findViewById(R.id.PhoneTextView);
                TextView usernameTextView = listItemView.findViewById(R.id.UsernameTextView);
                TextView filiereTextView = listItemView.findViewById(R.id.FiliereTextView);

                nameTextView.setText("Nom : " + name);
                emailTextView.setText("Email : " + email);
                phoneTextView.setText("Phone : " + String.valueOf(phone));
                usernameTextView.setText("Username : " + username);
                filiereTextView.setText("Filière : " + filiereName);

                etudiantListLayout.addView(listItemView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
