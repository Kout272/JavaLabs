$(document).ready(function() {
    LoadCountries();

    $('#countryEditForm').submit(function(event) {
        event.preventDefault();
        HandleCountryFormSubmit();
    });

    $('#codeForm').submit(function(event) {
        event.preventDefault();
        HandleCodeLookup();
    });

    $('#countryForm').submit(function(event) {
        event.preventDefault();
        HandleCountryLookup();
    });

    $('#refreshCountries').click(function() {
        LoadCountries();
    });

    $('#clearForm').click(function() {
        ResetCountryForm();
    });
});

function HandleCountryFormSubmit() {
    const countryData = {
        name: $('#name').val().trim(),
        code: $('#countryCode').val().trim()
    };

    if (!countryData.name || !countryData.code) {
        ShowAlert('Country name and code are required', 'danger');
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
            ShowAlert(`Country ${countryId ? 'updated' : 'created'} successfully`, 'success');
            LoadCountries();
            ResetCountryForm();
        },
        error: function(xhr) {
            const errorMsg = xhr.responseJSON?.message || xhr.responseText || 'Server error';
            ShowAlert(`Error: ${errorMsg}`, 'danger');
            console.error('Error details:', xhr);
        }
    });
}

function HandleCodeLookup() {
    const countryName = $('#countryName').val();
    $.get(`/api/countries/code/${encodeURIComponent(countryName)}`)
        .done(function(data) {
            $('#codeResult').html(`<div class="alert alert-success">Code: ${data}</div>`);
        })
        .fail(function() {
            $('#codeResult').html('<div class="alert alert-danger">Country not found</div>');
        });
}

function HandleCountryLookup() {
    const code = $('#code').val();
    $.get(`/api/countries/country/${encodeURIComponent(code)}`)
        .done(function(data) {
            $('#countryResult').html(`<div class="alert alert-success">Country: ${data}</div>`);
        })
        .fail(function() {
            $('#countryResult').html('<div class="alert alert-danger">Code not found</div>');
        });
}

function LoadCountries() {
    $.get('/api/countries')
        .done(function(countries) {
            RenderCountriesTable(countries);
        })
        .fail(function() {
            $('#countriesTable').html(
                '<tr><td colspan="4" class="text-center text-danger">Error loading countries</td></tr>'
            );
        });
}

function RenderCountriesTable(countries) {
    const tableBody = $('#countriesTable');
    tableBody.empty();

    if (countries.length === 0) {
        tableBody.append('<tr><td colspan="4" class="text-center">No countries found</td></tr>');
        return;
    }

    countries.forEach(function(country) {
        const row = `
            <tr>
                <td>${country.id}</td>
                <td>${country.name}</td>
                <td>${country.code}</td>
                <td>
                    <button onclick="EditCountry(${country.id})" class="btn btn-sm btn-warning">Edit</button>
                    <button onclick="DeleteCountry(${country.id})" class="btn btn-sm btn-danger">Delete</button>
                </td>
            </tr>
        `;
        tableBody.append(row);
    });
}

function EditCountry(id) {
    $.get(`/api/countries/${id}`)
        .done(function(country) {
            $('#countryId').val(country.id);
            $('#name').val(country.name);
            $('#countryCode').val(country.code);
            $('html, body').animate({ scrollTop: $('#countryEditForm').offset().top }, 'slow');
        })
        .fail(function() {
            ShowAlert('Error loading country data', 'danger');
        });
}

function DeleteCountry(id) {
    if (confirm('Are you sure you want to delete this country?')) {
        $.ajax({
            url: `/api/countries/${id}`,
            type: 'DELETE',
            success: function() {
                ShowAlert('Country deleted successfully', 'success');
                LoadCountries();
            },
            error: function() {
                ShowAlert('Error deleting country', 'danger');
            }
        });
    }
}

function ResetCountryForm() {
    $('#countryEditForm')[0].reset();
    $('#countryId').val('');
}

function ShowAlert(message, type) {
    const alert = $(`
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `);

    $('.container').prepend(alert);

    setTimeout(function() {
        alert.alert('close');
    }, 5000);
}
//