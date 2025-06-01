$(document).ready(function() {
    loadCountries();

    $('#countryEditForm').submit(function(e) {
        e.preventDefault();
        const countryData = {
            name: $('#name').val().trim(),
            code: $('#countryCode').val().trim()
        };

        if (!countryData.name || !countryData.code) {
            showAlert('Country name and code are required', 'danger');
            return;
        }

        const countryId = $('#countryId').val();
        const url = countryId ? `/api/countries/${countryId}` : '/api/countries';
        const method = countryId ? 'PUT' : 'POST';

        $.ajax({
            url: url,
            type: method,
            contentType: 'application/json',
            data: JSON.stringify(countryData),
            success: function(response) {
                showAlert(`Country ${countryId ? 'updated' : 'created'} successfully`, 'success');
                loadCountries();
                $('#countryEditForm')[0].reset();
                $('#countryId').val('');
            },
            error: function(xhr) {
                const errorMsg = xhr.responseJSON?.message ||
                    xhr.responseText ||
                    'Server error';
                showAlert(`Error: ${errorMsg}`, 'danger');
                console.error("Error details:", xhr);
            }
        });
    });

    $('#codeForm').submit(function(e) {
        e.preventDefault();
        const countryName = $('#countryName').val();
        $.get(`/api/countries/code/${encodeURIComponent(countryName)}`)
            .done(function(data) {
                $('#codeResult').html(`<div class="alert alert-success">Code: ${data}</div>`);
            })
            .fail(function() {
                $('#codeResult').html('<div class="alert alert-danger">Country not found</div>');
            });
    });

    $('#countryForm').submit(function(e) {
        e.preventDefault();
        const code = $('#code').val();
        $.get(`/api/countries/country/${encodeURIComponent(code)}`)
            .done(function(data) {
                $('#countryResult').html(`<div class="alert alert-success">Country: ${data}</div>`);
            })
            .fail(function() {
                $('#countryResult').html('<div class="alert alert-danger">Code not found</div>');
            });
    });

    $('#refreshCountries').click(function() {
        loadCountries();
    });

    $('#clearForm').click(function() {
        $('#countryEditForm')[0].reset();
        $('#countryId').val('');
    });


        const countryId = $('#countryId').val();
        const url = countryId ? `/api/countries/${countryId}` : '/api/countries';
        const method = countryId ? 'PUT' : 'POST';

        $.ajax({
            url: url,
            type: method,
            contentType: 'application/json',
            data: JSON.stringify(countryData),
            success: function(response) {
                showAlert(`Country ${countryId ? 'updated' : 'created'} successfully`, 'success');
                loadCountries();
                $('#countryEditForm')[0].reset();
                $('#countryId').val('');
            },
            error: function(xhr) {
                showAlert(`Error: ${xhr.responseJSON?.message || 'Server error'}`, 'danger');
            }
        });
    });

function loadCountries() {
    $.get('/api/countries')
        .done(function(countries) {
            const tableBody = $('#countriesTable');
            tableBody.empty();

            if (countries.length === 0) {
                tableBody.append('<tr><td colspan="4" class="text-center">No countries found</td></tr>');
                return;
            }

            countries.forEach(country => {
                const row = `
                    <tr>
                        <td>${country.id}</td>
                        <td>${country.name}</td>
                        <td>${country.code}</td>
                        <td>
                            <button onclick="editCountry(${country.id})" class="btn btn-sm btn-warning">Edit</button>
                            <button onclick="deleteCountry(${country.id})" class="btn btn-sm btn-danger">Delete</button>
                        </td>
                    </tr>
                `;
                tableBody.append(row);
            });
        })
        .fail(function() {
            $('#countriesTable').html('<tr><td colspan="4" class="text-center text-danger">Error loading countries</td></tr>');
        });
}

function editCountry(id) {
    $.get(`/api/countries/${id}`)
        .done(function(country) {
            $('#countryId').val(country.id);
            $('#name').val(country.name);
            $('#countryCode').val(country.code);
            $('html, body').animate({ scrollTop: $('#countryEditForm').offset().top }, 'slow');
        })
        .fail(function() {
            showAlert('Error loading country data', 'danger');
        });
}

function deleteCountry(id) {
    if (confirm('Are you sure you want to delete this country?')) {
        $.ajax({
            url: `/api/countries/${id}`,
            type: 'DELETE',
            success: function() {
                showAlert('Country deleted successfully', 'success');
                loadCountries();
            },
            error: function() {
                showAlert('Error deleting country', 'danger');
            }
        });
    }
}

function showAlert(message, type) {
    const alert = $(`<div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`);

    $('.container').prepend(alert);

    setTimeout(() => {
        alert.alert('close');
    }, 5000);
}