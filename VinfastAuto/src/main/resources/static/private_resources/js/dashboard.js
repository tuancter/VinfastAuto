// ================== TIỆN ÍCH CHUNG ==================
let chartInstance = null;
let currentPage = 0, itemsPerPage = 10;
let currentSearch = "", currentSort = "first_name", currentDirection = "asc";
let lastAddedUserIds = [];

const spinner = document.getElementById("spinner");
function showSpinner() { spinner.style.display = "flex"; }
function hideSpinner() { spinner.style.display = "none"; }
function getToken() { return localStorage.getItem("token"); }

function showAlert(message, type = "info") {
  const alertId = "alert-" + Date.now();
  const container = document.getElementById("alertContainer");
  const alert = document.createElement("div");
  alert.id = alertId;
  alert.className = `alert alert-${type} alert-dismissible fade show`;
  alert.role = "alert";
  alert.innerHTML = `
    ${message}
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  `;
  container.appendChild(alert);
  setTimeout(() => {
    const alertEl = document.getElementById(alertId);
    if (alertEl) {
      bootstrap.Alert.getOrCreateInstance(alertEl).close();
    }
  }, 4000);
}

function handleUnauthorized(res) {
    if (res.status === 401) {
        localStorage.removeItem("token");
        location.href = "/public_resources/login.html";
        return null;
    }
    return res.json();
}

// ================== XÁC THỰC & PHÂN QUYỀN ==================
function checkAdminRole() {
  fetch("/users/myRole", {
    headers: { "Authorization": "Bearer " + getToken() }
  })
  .then(res => {
    if (res.status === 401 || res.status === 403) {
      localStorage.removeItem("token");
      location.href = "/public_resources/login.html";
      return null;
    }
    return res.json();
  })
  .then(data => {
    if (!data) return;
    if (data.statusCode !== 2000) {
      alert("Không thể xác thực quyền.");
      localStorage.removeItem("token");
      location.href = "/public_resources/login.html";
      return;
    }
    const roles = data.data.roles || [];
    const hasAdminRole = roles.some(role => role.name === "ADMIN");
    if (!hasAdminRole) {
      alert("Bạn không có quyền truy cập trang này.");
      localStorage.removeItem("token");
      location.href = "/public_resources/login.html";
    } else {
      loadWelcome();
    }
  })
  .catch(() => {
    alert("Lỗi kết nối máy chủ.");
    localStorage.removeItem("token");
    location.href = "/public_resources/login.html";
  });
}
checkAdminRole();

function logout() {
    const token = localStorage.getItem("token");
    fetch('/auth/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token })
    })
    .then(response => response.json())
    .then(res => {
        if (res.statusCode === 1000) {
            localStorage.removeItem("token");
            location.href = "/public_resources/login.html";
        } else {
            alert("Lỗi: Đăng xuất thất bại!");
        }
    })
    .catch(() => alert("Lỗi kết nối máy chủ."));
}

// ================== HIỂN THỊ THÔNG TIN NGƯỜI DÙNG ĐĂNG NHẬP ==================
fetch("/users/myInfo", {
    headers: { "Authorization": "Bearer " + getToken() }
})
.then(handleUnauthorized)
.then(data => {
    if (!data) return;
    document.getElementById("currentUsername").innerText = "Xin chào, " + data.data.username;
});

function loadWelcome() {
    document.getElementById("pageTitle").innerText = "Trang chào mừng";
    document.getElementById("mainContent").innerHTML = "<p>Chào mừng bạn đến với hệ thống quản lý người dùng.</p>";
    $('#carContent').hide();
    $('#mainContent').show();
    setActiveMenu('menu-welcome');
}

// ================== QUẢN LÝ NGƯỜI DÙNG (CRUD, TÌM KIẾM, PHÂN TRANG) ==================
function loadUserList() {
    document.getElementById("pageTitle").innerText = "Quản lý người dùng";
    $('#carContent').hide();
    $('#mainContent').show();
    renderUserTable();
}

function fetchUsers(page, size, keyword, sortBy, direction) {
    showSpinner();
    const params = new URLSearchParams();
    if (keyword) params.append("keyword", keyword);
    params.append("page", page);
    params.append("size", size);
    params.append("sortBy", sortBy);
    params.append("direction", direction);

    return fetch("/users/search?" + params.toString(), {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(handleUnauthorized)
    .finally(hideSpinner);
}

function renderPagination(totalPages) {
    if (totalPages <= 1) return "";
    let html = '<nav><ul class="pagination justify-content-center">';

    // Nút trang đầu
    html += `<li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="goToPage(0)" aria-label="First">
            <i class="bi bi-chevron-double-left"></i>
        </a>
    </li>`;

    // Nút lùi 1 trang
    html += `<li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="goToPage(${currentPage - 1})" aria-label="Previous">
            <i class="bi bi-chevron-left"></i>
        </a>
    </li>`;

    // Luôn hiện trang đầu
    html += `<li class="page-item ${currentPage === 0 ? 'active' : ''}">
        <a class="page-link" href="#" onclick="goToPage(0)">1</a>
    </li>`;

    // Nếu cần dấu ...
    let start = Math.max(1, currentPage - 1);
    let end = Math.min(totalPages - 2, currentPage + 1);

    if (start > 1) {
        html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
    }

    for (let i = start; i <= end; i++) {
        html += `<li class="page-item ${i === currentPage ? 'active' : ''}">
            <a class="page-link" href="#" onclick="goToPage(${i})">${i + 1}</a>
        </li>`;
    }

    if (end < totalPages - 2) {
        html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
    }

    // Luôn hiện trang cuối nếu > 1 trang
    if (totalPages > 1) {
        html += `<li class="page-item ${currentPage === totalPages - 1 ? 'active' : ''}">
            <a class="page-link" href="#" onclick="goToPage(${totalPages - 1})">${totalPages}</a>
        </li>`;
    }

    // Nút tiến 1 trang
    html += `<li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="goToPage(${currentPage + 1})" aria-label="Next">
            <i class="bi bi-chevron-right"></i>
        </a>
    </li>`;

    // Nút trang cuối
    html += `<li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="goToPage(${totalPages - 1})" aria-label="Last">
            <i class="bi bi-chevron-double-right"></i>
        </a>
    </li>`;

    html += '</ul></nav>';
    return html;
}

function goToPage(page) {
    if (page < 0) page = 0;
    currentPage = page;
    renderUserTable();
}

function renderUserTable() {
    fetchUsers(currentPage, itemsPerPage, currentSearch, currentSort, currentDirection)
    .then(data => {
        if (!data) return;
        const users = data.data.content;
        const totalPages = data.data.totalPages;

        let html = `
<div class="mb-3 d-flex justify-content-between align-items-center">
    <div class="d-flex">
        <input style="width: 400px" type="text" id="searchInput" class="form-control me-2" placeholder="Tìm theo username..." value="${currentSearch}">
        <button class="btn btn-primary me-2" onclick="searchUsers()"><i class="bi bi-search"></i></button>
        ${currentSearch ? `<button class="btn btn-secondary" onclick="clearSearch()"><i class="bi bi-x"></i></button>` : ""}
        <button class="btn btn-success ms-2" onclick="openCreateUserModal()"><i class="bi bi-person-plus-fill"></i></button>
    </div>
    <div>
        <button class="btn btn-outline-primary me-2" onclick="openImportModal()"><i class="bi bi-upload"></i> Nhập file</button>
        <button class="btn btn-outline-secondary me-2" onclick="exportUsers('csv')"><i class="bi bi-download"></i> Xuất CSV</button>
        <button class="btn btn-outline-success" onclick="exportUsers('excel')"><i class="bi bi-file-earmark-excel"></i> Xuất Excel</button>
    </div>
</div>
<div class="mb-2 d-flex align-items-center">
            <label class="me-2 mb-0">Sắp xếp theo:</label>
            <select onchange="changeSort(this.value)" class="form-select w-auto me-3">
                <option value="first_name" ${currentSort === 'first_name' ? 'selected' : ''}>Tên</option>
                <option value="created_date" ${currentSort === 'created_date' ? 'selected' : ''}>Ngày tạo</option>
            </select>
            <select onchange="changeDirection(this.value)" class="form-select w-auto">
                <option value="asc" ${currentDirection === 'asc' ? 'selected' : ''}>Tăng dần</option>
                <option value="desc" ${currentDirection === 'desc' ? 'selected' : ''}>Giảm dần</option>
            </select>
        </div>

        <table class="table table-bordered"><thead><tr>
    <th>Tài khoản</th>
    <th>Tên</th>
    <th>Họ</th>
    <th>Ngày sinh</th>
    <th>Số điện thoại</th>
    <th>Email</th>
    <th>Vị trí</th>
    <th>Ngày tạo</th>
    <th>Hành động</th>
</tr></thead><tbody>`;

        users.forEach(u => {
            const highlightClass = lastAddedUserIds.includes(u.id) ? "table-success" : "";
            html += `<tr class="${highlightClass}">
                <td>${u.username}</td>
                <td>${u.firstName}</td>
                <td>${u.lastName}</td>
                <td>${u.birthday || ""}</td>
                <td>${u.mobilephone || ""}</td>
                <td>${u.email}</td>
                <td>${u.position || ""}</td>
                <td>${u.createdDate || ""}</td>
                <td>
                  <button class="btn btn-warning btn-sm" onclick="editUser('${u.id}')"><i class="bi bi-pencil-fill"></i></button>
                  <button class="btn btn-danger btn-sm" onclick="deleteUser('${u.id}')"><i class="bi bi-trash-fill"></i></button>
                  <button class="btn btn-info btn-sm" onclick="editUserRoles('${u.id}', '${u.username}')"><i class="bi bi-shield-lock-fill"></i></button>
                </td>`;
        });

        html += "</tbody></table>" + renderPagination(totalPages);
        document.getElementById("mainContent").innerHTML = html;
    });
}

function searchUsers() {
    const searchInput = document.getElementById("searchInput").value.trim();
    currentSearch = searchInput;
    currentPage = 0;
    renderUserTable();
}

function clearSearch() {
    currentSearch = "";
    currentPage = 0;
    document.getElementById("searchInput").value = "";
    renderUserTable();
}

function changeSort(sortBy) {
    currentSort = sortBy || "first_name";
    currentPage = 0;
    renderUserTable();
}

function changeDirection(direction) {
    currentDirection = direction || "asc";
    currentPage = 0;
    renderUserTable();
}

// ================== QUẢN LÝ NGƯỜI DÙNG: TẠO, SỬA, XOÁ ==================

// Danh sách tỉnh thành Việt Nam
const provincesVN = [
  "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre",
  "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk",
  "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh",
  "Hải Dương", "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum",
  "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình",
  "Ninh Thuận", "Phú Thọ", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị",
  "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế",
  "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái", "Phú Yên", "Cần Thơ",
  "Đà Nẵng", "Hải Phòng", "Hà Nội", "TP Hồ Chí Minh"
];

// Điền options cho select vị trí
function fillProvinceOptions(selected = "") {
    const select = document.getElementById("position");
    select.innerHTML = provincesVN.map(p => `<option value="${p}" ${p === selected ? "selected" : ""}>${p}</option>`).join("");
}

// Mở modal thêm người dùng
function openCreateUserModal() {
    document.getElementById("formMode").value = "create";
    document.getElementById("userModalLabel").innerText = "Thêm người dùng";
    document.getElementById("userForm").reset();
    document.getElementById("username").readOnly = false;
    document.getElementById("password").required = true;
    fillProvinceOptions();
    document.getElementById("passwordHint").style.display = "none";
    // Xóa gợi ý sửa mật khẩu nếu có
    const passwordHelp = document.getElementById("passwordHelp");
    if (passwordHelp) passwordHelp.remove();
    new bootstrap.Modal(document.getElementById("userModal")).show();
}

// Mở modal sửa người dùng
function editUser(userId) {
    // Gọi API lấy user mới nhất
    fetch(`/users/${userId}`, {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(handleUnauthorized)
    .then(res => {
        if (!res || res.statusCode !== 2000) {
            showAlert("Không tìm thấy người dùng.", "warning");
            return;
        }
        const user = res.data;
        document.getElementById("formMode").value = "edit";
        document.getElementById("userModalLabel").innerText = "Sửa người dùng";
        document.getElementById("userId").value = user.id;
        document.getElementById("username").value = user.username;
        document.getElementById("username").readOnly = true;
        document.getElementById("firstName").value = user.firstName;
        document.getElementById("lastName").value = user.lastName;
        document.getElementById("birthday").value = user.birthday ? user.birthday.split("T")[0] : "";
        fillProvinceOptions(user.position || "");
        document.getElementById("email").value = user.email;
        document.getElementById("password").value = "";
        document.getElementById("password").required = false;
        document.getElementById("mobilephone").value = user.mobilephone || "";
      
        // Hiện hint mật khẩu
        document.getElementById("passwordHint").style.display = "block";
        new bootstrap.Modal(document.getElementById("userModal")).show();
    });
}

// Xoá người dùng
function deleteUser(userId) {
    if (!confirm("Bạn có chắc chắn muốn xoá người dùng này?")) return;
    showSpinner();
    fetch(`/users/${userId}`, {
        method: "DELETE",
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(handleUnauthorized)
    .then(res => {
        hideSpinner();
        if (res && res.statusCode === 2000) {
            showAlert("Xoá người dùng thành công!", "success");
            renderUserTable();
        } else {
            showAlert(res?.message || "Xoá thất bại!", "danger");
        }
    })
    .catch(() => {
        hideSpinner();
        showAlert("Lỗi kết nối máy chủ.", "danger");
    });
}

// Xử lý submit form tạo/sửa user
document.getElementById("userForm").addEventListener("submit", function(e) {
    e.preventDefault();
    const mode = document.getElementById("formMode").value;
    const userId = document.getElementById("userId").value;
    const data = {
        username: document.getElementById("username").value.trim(),
        password: document.getElementById("password").value,
        firstName: document.getElementById("firstName").value.trim(),
        lastName: document.getElementById("lastName").value.trim(),
        birthday: document.getElementById("birthday").value,
        position: document.getElementById("position").value,
        email: document.getElementById("email").value.trim(),
        mobilephone: document.getElementById("mobilephone").value.trim()
    };

    // Validate cơ bản
    if (!data.username || !data.firstName || !data.lastName || !data.email || !data.birthday || !data.position) {
        showAlert("Vui lòng nhập đầy đủ thông tin!", "warning");
        return;
    }
    if (mode === "create" && (!data.password || data.password.length < 8)) {
        showAlert("Mật khẩu phải có ít nhất 8 ký tự!", "warning");
        return;
    }

    showSpinner();

    let url = "/users";
    let method = "POST";
    if (mode === "edit") {
        url = `/users/${userId}`;
        method = "PUT";
        if (!data.password) delete data.password; // Nếu không đổi mật khẩu thì bỏ trường này
    }

    fetch(url, {
        method,
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getToken()
        },
        body: JSON.stringify(data)
    })
    .then(handleUnauthorized)
    .then(res => {
        hideSpinner();
        if (res && res.statusCode === 2000) {
            // Highlight user vừa thêm
            if (mode === "create" && res.data && res.data.id) {
                lastAddedUserIds = [res.data.id];
            }
            showAlert(mode === "create" ? "Thêm người dùng thành công!" : "Cập nhật thành công!", "success");
            bootstrap.Modal.getInstance(document.getElementById("userModal")).hide();
            renderUserTable();
            // Dòng này để tự động xoá highlight
            // setTimeout(() => { lastAddedUserIds = []; renderUserTable(); }, 5000);
        } else {
            showAlert(res?.message || "Thao tác thất bại!", "danger");
        }
    })
    .catch(() => {
        hideSpinner();
        showAlert("Lỗi kết nối máy chủ.", "danger");
    });
});

// ================== IMPORT/EXPORT NGƯỜI DÙNG ==================

// Mở modal import
function openImportModal() {
    document.getElementById("importForm").reset();
    new bootstrap.Modal(document.getElementById("importModal")).show();
}

// Xử lý import file
document.getElementById("importForm").addEventListener("submit", function(e) {
    e.preventDefault();
    const fileInput = document.getElementById("importFile");
    if (!fileInput.files.length) {
        showAlert("Vui lòng chọn file.", "warning");
        return;
    }
    const file = fileInput.files[0];
    const reader = new FileReader();
    reader.onload = function(event) {
        let users = [];
        if (file.name.endsWith(".csv")) {
            // Parse CSV
            const text = event.target.result;
            users = parseCSVUsers(text);
        } else if (file.name.endsWith(".xlsx")) {
            // Parse Excel
            const data = new Uint8Array(event.target.result);
            const workbook = XLSX.read(data, { type: "array" });
            const sheet = workbook.Sheets[workbook.SheetNames[0]];
            users = XLSX.utils.sheet_to_json(sheet, { defval: "" });
        } else {
            showAlert("Định dạng file không hỗ trợ.", "danger");
            return;
        }
        importUsersBatch(users);
    };
    if (file.name.endsWith(".csv")) {
        reader.readAsText(file, "UTF-8");
    } else {
        reader.readAsArrayBuffer(file);
    }
});

// Hàm parse CSV thành mảng user object
function parseCSVUsers(csvText) {
    const lines = csvText.split(/\r?\n/).filter(line => line.trim());
    if (lines.length < 2) return [];
    const headers = lines[0].split(",").map(h => h.trim());
    return lines.slice(1).map(line => {
        const values = line.split(",");
        const obj = {};
        headers.forEach((h, i) => obj[h] = values[i] ? values[i].trim() : "");
        return obj;
    });
}

// Hàm import từng user (POST /users)
function importUsersBatch(users) {
    if (!Array.isArray(users) || users.length === 0) {
        showAlert("Không có dữ liệu hợp lệ.", "warning");
        return;
    }
    let success = 0, fail = 0;
    let done = 0;
    let addedIds = [];
    showSpinner();
    users.forEach(user => {
        // Map field cho đúng backend, có thể cần chỉnh lại tên field cho khớp
        const data = {
            username: user.username || user["Tên đăng nhập"] || "",
            password: (user.password || user["Mật khẩu"] || "12345678").toString(),
            firstName: user.firstName || user["Tên"] || "",
            lastName: user.lastName || user["Họ"] || "",
            birthday: user.birthday || user["Ngày sinh"] || "",
            position: user.position || user["Vị trí"] || "",
            mobilephone: user.mobilephone || user["Số điện thoại"] || "",
            email: user.email || user["Email"] || ""
        };
        // Validate cơ bản
        if (
            !data.username ||
            !data.firstName ||
            !data.lastName ||
            !data.email ||
            !data.birthday ||
            !data.position ||
            !data.password ||
            data.password.length < 8
        ) {
            fail++; done++;
            if (done === users.length) finishImport(success, fail);
            return;
        }
        fetch("/users", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + getToken()
            },
            body: JSON.stringify(data)
        })
        .then(handleUnauthorized)
        .then(res => {
            if (res && res.statusCode === 2000) {
                success++;
                if (res.data && res.data.id) addedIds.push(res.data.id);
            } else fail++;
        })
        .catch(() => { fail++; })
        .finally(() => {
            done++;
            if (done === users.length) finishImport(success, fail, addedIds);
        });
    });
}

function finishImport(success, fail, addedIds = []) {
    hideSpinner();
    showAlert(`Nhập thành công ${success} người dùng. Thất bại: ${fail}.`, success > 0 ? "success" : "danger");
    bootstrap.Modal.getInstance(document.getElementById("importModal")).hide();
    if (success > 0) {
        lastAddedUserIds = addedIds;
        renderUserTable();
        // Dòng này để tự động xoá highlight
        // setTimeout(() => { lastAddedUserIds = []; renderUserTable(); }, 2000);
    }
}

// Xuất danh sách người dùng ra file
function exportUsers(type) {
    showSpinner();
    // Lấy toàn bộ user (không phân trang)
    fetchUsers(0, 10000, "", currentSort, currentDirection)
    .then(data => {
        hideSpinner();
        if (!data || !data.data || !data.data.content) {
            showAlert("Không có dữ liệu để xuất.", "warning");
            return;
        }
        const users = data.data.content;
        if (type === "csv") {
            exportUsersToCSV(users);
        } else {
            exportUsersToExcel(users);
        }
    });
}

// Xuất CSV
function exportUsersToCSV(users) {
    const headers = [
        "id", "username", "firstName", "lastName", "birthday",
        "position", "mobilephone", "email", "createdDate"
    ];
    const csvRows = [
        headers.join(","),
        ...users.map(u => headers.map(h => `"${(u[h] || "").replace(/"/g, '""')}"`).join(","))
    ];
    const blob = new Blob([csvRows.join("\r\n")], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "users.csv";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

// Xuất Excel
function exportUsersToExcel(users) {
    const headers = [
        "id", "username", "firstName", "lastName", "birthday",
        "position", "mobilephone", "email", "createdDate"
    ];
    // Đảm bảo đúng thứ tự cột
    const data = users.map(u => {
        const obj = {};
        headers.forEach(h => obj[h] = u[h] || "");
        return obj;
    });
    const ws = XLSX.utils.json_to_sheet(data, { header: headers });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Users");
    const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
    const blob = new Blob([wbout], { type: "application/octet-stream" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "users.xlsx";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

// ================== BIỂU ĐỒ THỐNG KÊ NGƯỜI DÙNG ==================
function renderUserChart(type = "position", chartStyle = "bar") {
    const ctx = document.getElementById("userChart");
    if (!ctx) {
        console.error("Phần tử canvas không tồn tại.");
        return;
    }

    showSpinner();

    let apiUrl = "";
    let chartTitle = "";
    switch (type) {
        case "position":
            apiUrl = "/users/statistics/by-position";
            chartTitle = "Người dùng theo vị trí";
            break;
        case "age":
            apiUrl = "/users/statistics/by-age-group";
            chartTitle = "Người dùng theo độ tuổi";
            break;
        case "year":
            apiUrl = "/users/statistics/by-year";
            chartTitle = "Người dùng theo năm tạo";
            break;
        default:
            hideSpinner();
            console.error("Loại biểu đồ không hợp lệ.");
            return;
    }

    const token = localStorage.getItem("token");

    fetch(apiUrl, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(result => {
            hideSpinner();
            if (!result || !result.data) {
                showAlert("Không có dữ liệu thống kê.", "warning");
                return;
            }

            const labels = result.data.map(item => item.key);
            const dataSet = result.data.map(item => item.count);

            // Màu sắc đẹp hơn cho Pie/Doughnut
            const backgroundColors = [
                "#4e79a7", "#f28e2b", "#e15759", "#76b7b2", "#59a14f", "#edc949",
                "#af7aa1", "#ff9da7", "#9c755f", "#bab0ab", "#86b7fe", "#ffc107"
            ];

            if (chartInstance) chartInstance.destroy();

            chartInstance = new Chart(ctx, {
                type: chartStyle,
                data: {
                    labels: labels,
                    datasets: [{
                        label: "Số lượng người dùng",
                        data: dataSet,
                        backgroundColor: chartStyle === "bar" || chartStyle === "line"
                            ? "rgba(54, 162, 235, 0.6)"
                            : backgroundColors,
                        borderColor: chartStyle === "bar" || chartStyle === "line"
                            ? "rgba(54, 162, 235, 1)"
                            : "#fff",
                        borderWidth: 1,
                        fill: chartStyle === "line" ? true : false,
                        tension: chartStyle === "line" ? 0.4 : 0
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: {
                            display: true,
                            text: chartTitle,
                            font: { size: 18 }
                        },
                        legend: {
                            display: chartStyle !== "bar" && chartStyle !== "line",
                            position: "bottom"
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    if (chartStyle === "pie" || chartStyle === "doughnut") {
                                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                        const value = context.parsed;
                                        const percent = ((value / total) * 100).toFixed(1);
                                        return `${context.label}: ${value} (${percent}%)`;
                                    }
                                    return `${context.label}: ${context.parsed.y ?? context.parsed}`;
                                }
                            }
                        }
                    },
                    scales: (chartStyle === "bar" || chartStyle === "line") ? {
                        x: { beginAtZero: true, grid: { display: false } },
                        y: { beginAtZero: true, precision: 0 }
                    } : {}
                }
            });
        })
        .catch(error => {
            hideSpinner();
            console.error("Lỗi khi gọi API thống kê:", error);
            showAlert("Lỗi kết nối máy chủ. Không thể tải dữ liệu thống kê.", "danger");
        });
}

function updateUserChart() {
    const type = document.getElementById("chartTypeSelect").value;
    const chartStyle = document.getElementById("chartStyleSelect").value;
    renderUserChart(type, chartStyle);
}

function loadUserChart() {
    document.getElementById("pageTitle").innerText = "Thống kê người dùng";
    document.getElementById("mainContent").innerHTML = `
        <div class="chart-container">
            <h5>Thống kê người dùng</h5>
            <div class="mb-2 d-flex align-items-center">
                <select id="chartTypeSelect" class="form-select w-auto me-2" onchange="updateUserChart()">
                    <option value="position">Theo vị trí</option>
                    <option value="age">Theo độ tuổi</option>
                    <option value="year">Theo năm tạo</option>
                </select>
                <select id="chartStyleSelect" class="form-select w-auto me-2" onchange="updateUserChart()">
                    <option value="bar">Cột</option>
                    <option value="pie">Tròn</option>
                    <option value="doughnut">Doughnut</option>
                    <option value="line">Đường</option>
                </select>
                <button class="btn btn-outline-success ms-2" onclick="downloadChartImage()">
                    <i class="bi bi-download"></i> Tải ảnh biểu đồ
                </button>
            </div>
            <canvas id="userChart" width="350" height="180"></canvas>
        </div>
    `;
    $('#carContent').hide();
    $('#mainContent').show();
    renderUserChart();
}

// Xuất ảnh biểu đồ
function downloadChartImage() {
    if (!chartInstance) {
        showAlert("Chưa có biểu đồ để xuất.", "warning");
        return;
    }
    const link = document.createElement("a");
    link.href = chartInstance.toBase64Image();
    link.download = "user_chart.png";
    link.click();
}

// ================== QUẢN LÝ VAI TRÒ NGƯỜI DÙNG ==================
const roles = [
  { id: 1, name: "ADMIN" },
  { id: 2, name: "USER" },
  { id: 3, name: "MANAGER" }
];

function editUserRoles(userId, username) {
  document.getElementById("roleUserId").value = userId;
  document.getElementById("roleUsername").value = username;

  showSpinner();

  fetch(`/users/${userId}/roles`, {
    headers: { "Authorization": "Bearer " + getToken() }
  })
    .then(handleUnauthorized)
    .then(res => {
      hideSpinner();
      if (!res || res.statusCode !== 2000 || !res.data) return;

      const userRoleIds = (res.data.roles || []).map(role => role.id);

      const checkboxes = roles.map(role => {
        const isChecked = userRoleIds.includes(role.id);
        return `
          <div class="form-check">
            <input class="form-check-input" type="checkbox" value="${role.id}" id="role_${role.id}" ${isChecked ? "checked" : ""}>
            <label class="form-check-label" for="role_${role.id}">${role.name}</label>
          </div>
        `;
      }).join("");

      document.getElementById("roleCheckboxes").innerHTML = checkboxes;

      new bootstrap.Modal(document.getElementById("roleModal")).show();
    })
    .catch(() => {
      hideSpinner();
      showAlert("Lỗi khi tải vai trò.", "danger");
    });
}

document.getElementById("roleForm").addEventListener("submit", function (e) {
  e.preventDefault();

  const userId = document.getElementById("roleUserId").value;
  const checkboxes = document.querySelectorAll("#roleCheckboxes input[type=checkbox]");
  const selectedRoleIds = Array.from(checkboxes)
    .filter(cb => cb.checked)
    .map(cb => Number(cb.value));

  showSpinner();

  fetch(`/users/${userId}/roles`, {
    method: "PUT",
    headers: {
      "Authorization": "Bearer " + getToken(),
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ roleIds: selectedRoleIds })
  })
    .then(handleUnauthorized)
    .then(res => {
      hideSpinner();
      if (res && res.statusCode === 2000) {
        showAlert("Cập nhật vai trò thành công!", "success");
        bootstrap.Modal.getInstance(document.getElementById("roleModal")).hide();
      } else {
        showAlert(res?.message || "Không thể cập nhật vai trò.", "danger");
      }
    })
    .catch(() => {
      hideSpinner();
      showAlert("Lỗi kết nối máy chủ.", "danger");
    });
});

// ========== Quản lý xe ô tô (phân trang, tìm kiếm, sắp xếp, CRUD) ==========
let currentCarPage = 0, carItemsPerPage = 10;
let carSearch = "", carSort = "name", carDirection = "asc";
let carSearchResults = null;

function loadCarList() {
    $('#pageTitle').text('Quản lý xe ô tô');
    $('#mainContent').hide();
    $('#carContent').show();
    renderCarTable();
}

function fetchCars(page, size, keyword, sortBy, direction) {
    showSpinner();
    const params = new URLSearchParams();
    if (keyword) params.append("keyword", keyword);
    params.append("page", page);
    params.append("size", size);
    params.append("sortBy", sortBy);
    params.append("direction", direction);
    return fetch("/cars/search?" + params.toString(), {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(handleUnauthorized)
    .finally(hideSpinner);
}

function renderCarPagination(totalPages) {
    if (totalPages <= 1) return "";
    let html = '<nav><ul class="pagination justify-content-center">';
    html += `<li class="page-item ${currentCarPage === 0 ? 'disabled' : ''}"><a class="page-link" href="#" onclick="goToCarPage(0)" aria-label="First"><i class="bi bi-chevron-double-left"></i></a></li>`;
    html += `<li class="page-item ${currentCarPage === 0 ? 'disabled' : ''}"><a class="page-link" href="#" onclick="goToCarPage(${currentCarPage - 1})" aria-label="Previous"><i class="bi bi-chevron-left"></i></a></li>`;
    html += `<li class="page-item ${currentCarPage === 0 ? 'active' : ''}"><a class="page-link" href="#" onclick="goToCarPage(0)">1</a></li>`;
    let start = Math.max(1, currentCarPage - 1);
    let end = Math.min(totalPages - 2, currentCarPage + 1);
    if (start > 1) html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
    for (let i = start; i <= end; i++) {
        html += `<li class="page-item ${i === currentCarPage ? 'active' : ''}"><a class="page-link" href="#" onclick="goToCarPage(${i})">${i + 1}</a></li>`;
    }
    if (end < totalPages - 2) html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
    if (totalPages > 1) html += `<li class="page-item ${currentCarPage === totalPages - 1 ? 'active' : ''}"><a class="page-link" href="#" onclick="goToCarPage(${totalPages - 1})">${totalPages}</a></li>`;
    html += `<li class="page-item ${currentCarPage === totalPages - 1 ? 'disabled' : ''}"><a class="page-link" href="#" onclick="goToCarPage(${currentCarPage + 1})" aria-label="Next"><i class="bi bi-chevron-right"></i></a></li>`;
    html += `<li class="page-item ${currentCarPage === totalPages - 1 ? 'disabled' : ''}"><a class="page-link" href="#" onclick="goToCarPage(${totalPages - 1})" aria-label="Last"><i class="bi bi-chevron-double-right"></i></a></li>`;
    html += '</ul></nav>';
    return html;
}

function goToCarPage(page) {
    if (page < 0) page = 0;
    currentCarPage = page;
    renderCarTable();
}

function formatCurrency(value) {
    if (value == null || isNaN(value)) return '';
    return Number(value).toLocaleString('vi-VN') + 'đ';
}

function renderCarTable() {
    if (carSearchResults !== null) {
        // Sắp xếp client-side
        let sortedCars = [...carSearchResults];
        if (carSort === 'name') {
            sortedCars.sort((a, b) => {
                if (!a.name) return 1;
                if (!b.name) return -1;
                return carDirection === 'asc'
                    ? a.name.localeCompare(b.name, 'vi', { sensitivity: 'base' })
                    : b.name.localeCompare(a.name, 'vi', { sensitivity: 'base' });
            });
        } else if (carSort === 'price') {
            sortedCars.sort((a, b) => {
                let pa = Number(a.price) || 0;
                let pb = Number(b.price) || 0;
                return carDirection === 'asc' ? pa - pb : pb - pa;
            });
        } else if (carSort === 'manufacturedYear') {
            sortedCars.sort((a, b) => {
                let ya = Number(a.manufacturedYear) || 0;
                let yb = Number(b.manufacturedYear) || 0;
                return carDirection === 'asc' ? ya - yb : yb - ya;
            });
        }
        const total = sortedCars.length;
        const totalPages = Math.ceil(total / carItemsPerPage);
        const start = currentCarPage * carItemsPerPage;
        const end = start + carItemsPerPage;
        const cars = sortedCars.slice(start, end);
        let html = `
<div class="mb-3 d-flex justify-content-between align-items-center">
    <div class="d-flex">
        <input style="width: 400px" type="text" id="carSearchInput" class="form-control me-2" placeholder="Tìm theo tên xe..." value="${carSearch}">
        <button class="btn btn-primary me-2" onclick="searchCars()"><i class="bi bi-search"></i></button>
        ${carSearch ? `<button class="btn btn-secondary" onclick="clearCarSearch()"><i class="bi bi-x"></i></button>` : ""}
        <button class="btn btn-success ms-2" onclick="showCarModal('create')"><i class="bi bi-plus-circle"></i></button>
    </div>
    <div>
        <button class="btn btn-outline-primary me-2" onclick="openCarImportModal()"><i class="bi bi-upload"></i> Nhập file</button>
        <button class="btn btn-outline-secondary me-2" onclick="exportCars('csv')"><i class="bi bi-download"></i> Xuất CSV</button>
        <button class="btn btn-outline-success" onclick="exportCars('excel')"><i class="bi bi-file-earmark-excel"></i> Xuất Excel</button>
    </div>
</div>
<div class="mb-2 d-flex align-items-center">
            <label class="me-2 mb-0">Sắp xếp theo:</label>
            <select onchange="changeCarSort(this.value)" class="form-select w-auto me-3">
                <option value="name" ${carSort === 'name' ? 'selected' : ''}>Tên xe</option>
                <option value="price" ${carSort === 'price' ? 'selected' : ''}>Giá</option>
                <option value="manufacturedYear" ${carSort === 'manufacturedYear' ? 'selected' : ''}>Năm SX</option>
            </select>
            <select onchange="changeCarDirection(this.value)" class="form-select w-auto">
                <option value="asc" ${carDirection === 'asc' ? 'selected' : ''}>Tăng dần</option>
                <option value="desc" ${carDirection === 'desc' ? 'selected' : ''}>Giảm dần</option>
            </select>
        </div>
<table class="table table-bordered table-hover"><thead><tr>
    <th>Tên xe</th><th>Giá</th><th>Năm SX</th><th>Tình trạng</th><th>Km đã đi</th><th>Xuất xứ</th><th>Loại xe</th><th>Động cơ</th><th>Màu ngoại thất</th><th>Màu nội thất</th><th>Ghế</th><th>Cửa</th><th>Hành động</th>
</tr></thead><tbody>`;
        cars.forEach(car => {
            html += `<tr>
                <td>${car.name}</td><td>${formatCurrency(car.price)}</td><td>${car.manufacturedYear}</td><td>${car.state||''}</td><td>${car.mileage||''}</td><td>${car.origin||''}</td><td>${car.vehicleType||''}</td><td>${car.engine||''}</td><td>${car.exteriorColor||''}</td><td>${car.interiorColor||''}</td><td>${car.seats||''}</td><td>${car.doors||''}</td>
                <td>
                    <button class='btn btn-sm btn-primary' onclick='showCarModal("edit",${JSON.stringify(car)})'><i class='bi bi-pencil'></i></button>
                    <button class='btn btn-sm btn-danger' onclick='deleteCar(${car.carId})'><i class='bi bi-trash'></i></button>
                </td>
            </tr>`;
        });
        html += '</tbody></table>' + renderCarPagination(totalPages);
        $('#carContent').html(html);
        return;
    }
    // Nếu không phải search, gọi API phân trang như cũ
    fetchCars(currentCarPage, carItemsPerPage, carSearch, carSort, carDirection)
    .then(data => {
        if (!data) return;
        const cars = data.data.content;
        const totalPages = data.data.totalPages;
        let html = `
<div class="mb-3 d-flex justify-content-between align-items-center">
    <div class="d-flex">
        <input style="width: 400px" type="text" id="carSearchInput" class="form-control me-2" placeholder="Tìm theo tên xe..." value="${carSearch}">
        <button class="btn btn-primary me-2" onclick="searchCars()"><i class="bi bi-search"></i></button>
        ${carSearch ? `<button class="btn btn-secondary" onclick="clearCarSearch()"><i class="bi bi-x"></i></button>` : ""}
        <button class="btn btn-success ms-2" onclick="showCarModal('create')"><i class="bi bi-plus-circle"></i></button>
    </div>
    <div>
        <button class="btn btn-outline-primary me-2" onclick="openCarImportModal()"><i class="bi bi-upload"></i> Nhập file</button>
        <button class="btn btn-outline-secondary me-2" onclick="exportCars('csv')"><i class="bi bi-download"></i> Xuất CSV</button>
        <button class="btn btn-outline-success" onclick="exportCars('excel')"><i class="bi bi-file-earmark-excel"></i> Xuất Excel</button>
    </div>
</div>
<div class="mb-2 d-flex align-items-center">
            <label class="me-2 mb-0">Sắp xếp theo:</label>
            <select onchange="changeCarSort(this.value)" class="form-select w-auto me-3">
                <option value="name" ${carSort === 'name' ? 'selected' : ''}>Tên xe</option>
                <option value="price" ${carSort === 'price' ? 'selected' : ''}>Giá</option>
                <option value="manufacturedYear" ${carSort === 'manufacturedYear' ? 'selected' : ''}>Năm SX</option>
            </select>
            <select onchange="changeCarDirection(this.value)" class="form-select w-auto">
                <option value="asc" ${carDirection === 'asc' ? 'selected' : ''}>Tăng dần</option>
                <option value="desc" ${carDirection === 'desc' ? 'selected' : ''}>Giảm dần</option>
            </select>
        </div>
<table class="table table-bordered table-hover"><thead><tr>
    <th>Tên xe</th><th>Giá</th><th>Năm SX</th><th>Tình trạng</th><th>Km đã đi</th><th>Xuất xứ</th><th>Loại xe</th><th>Động cơ</th><th>Màu ngoại thất</th><th>Màu nội thất</th><th>Ghế</th><th>Cửa</th><th>Hành động</th>
</tr></thead><tbody>`;
        cars.forEach(car => {
            html += `<tr>
                <td>${car.name}</td><td>${formatCurrency(car.price)}</td><td>${car.manufacturedYear}</td><td>${car.state||''}</td><td>${car.mileage||''}</td><td>${car.origin||''}</td><td>${car.vehicleType||''}</td><td>${car.engine||''}</td><td>${car.exteriorColor||''}</td><td>${car.interiorColor||''}</td><td>${car.seats||''}</td><td>${car.doors||''}</td>
                <td>
                    <button class='btn btn-sm btn-primary' onclick='showCarModal("edit",${JSON.stringify(car)})'><i class='bi bi-pencil'></i></button>
                    <button class='btn btn-sm btn-danger' onclick='deleteCar(${car.carId})'><i class='bi bi-trash'></i></button>
                </td>
            </tr>`;
        });
        html += '</tbody></table>' + renderCarPagination(totalPages);
        $('#carContent').html(html);
    });
}

function searchCars() {
    const searchInput = document.getElementById("carSearchInput").value.trim().toLowerCase();
    carSearch = searchInput;
    currentCarPage = 0;
    // Nếu không có từ khoá, reset về fetch bình thường
    if (!carSearch) {
        carSearchResults = null;
        renderCarTable();
        return;
    }
    // Lấy toàn bộ xe từ backend (tối đa 10000 bản ghi)
    showSpinner();
    fetchCars(0, 10000, "", carSort, carDirection)
    .then(data => {
        hideSpinner();
        if (!data || !data.data || !data.data.content) {
            carSearchResults = [];
            renderCarTable();
            return;
        }
        // Lọc theo tên xe
        const allCars = data.data.content;
        const filtered = allCars.filter(car => car.name && car.name.toLowerCase().includes(carSearch));
        carSearchResults = filtered;
        if (filtered.length === 0) {
            showAlert('Không tìm thấy xe nào phù hợp!', 'warning');
        }
        renderCarTable();
    })
    .catch(() => {
        hideSpinner();
        showAlert('Lỗi khi tìm kiếm xe!', 'danger');
    });
}

function clearCarSearch() {
    carSearch = "";
    carSearchResults = null;
    currentCarPage = 0;
    $('#carSearchInput').val("");
    renderCarTable();
}

function changeCarSort(sortBy) {
    carSort = sortBy || "name";
    currentCarPage = 0;
    renderCarTable();
}

function changeCarDirection(direction) {
    carDirection = direction || "asc";
    currentCarPage = 0;
    renderCarTable();
}

function showCarModal(mode, car) {
    $('#carForm')[0].reset();
    $('#carFormMode').val(mode);
    if(mode==='edit' && car) {
        $('#carModalLabel').text('Sửa xe ô tô');
        $('#carId').val(car.carId);
        $('#carName').val(car.name);
        $('#carPrice').val(car.price);
        $('#carYear').val(car.manufacturedYear);
        $('#carState').val(car.state);
        $('#carMileage').val(car.mileage);
        $('#carOrigin').val(car.origin);
        $('#carType').val(car.vehicleType);
        $('#carEngine').val(car.engine);
        $('#carExteriorColor').val(car.exteriorColor);
        $('#carInteriorColor').val(car.interiorColor);
        $('#carSeats').val(car.seats);
        $('#carDoors').val(car.doors);
        $('#carImgLink').val(car.imgLink);
        $('#carDescription').val(car.description);
    } else {
        $('#carModalLabel').text('Thêm xe ô tô');
        $('#carId').val('');
    }
    var carModal = new bootstrap.Modal(document.getElementById('carModal'));
    carModal.show();
}

$('#carForm').off('submit').on('submit', handleCarFormSubmit);
function handleCarFormSubmit(e) {
    e.preventDefault();
    let mode = $('#carFormMode').val();
    let carId = $('#carId').val();
    let carData = {
        name: $('#carName').val(),
        price: $('#carPrice').val(),
        manufacturedYear: $('#carYear').val(),
        state: $('#carState').val(),
        mileage: $('#carMileage').val(),
        origin: $('#carOrigin').val(),
        vehicleType: $('#carType').val(),
        engine: $('#carEngine').val(),
        exteriorColor: $('#carExteriorColor').val(),
        interiorColor: $('#carInteriorColor').val(),
        seats: $('#carSeats').val(),
        doors: $('#carDoors').val(),
        imgLink: $('#carImgLink').val(),
        description: $('#carDescription').val()
    };
    showSpinner();
    let url = '/cars', method = 'POST';
    if(mode==='edit') {
        url = `/cars/${carId}`;
        method = 'PUT';
    }
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(carData)
    })
    .then(handleUnauthorized)
    .then(res => {
        hideSpinner();
        if(res && res.statusCode === 2000) {
            $('#carModal').modal('hide');
            loadCarList();
            showAlert(mode==='create' ? 'Thêm xe thành công!' : 'Cập nhật xe thành công!','success');
        } else {
            showAlert(res?.message || (mode==='create'?'Thêm xe thất bại!':'Cập nhật xe thất bại!'),'danger');
        }
    })
    .catch(()=>{ hideSpinner(); showAlert('Lỗi kết nối máy chủ!','danger'); });
}

function deleteCar(carId) {
    if(confirm('Bạn có chắc chắn muốn xóa xe này?')) {
        showSpinner();
        fetch(`/cars/${carId}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + getToken() }
        })
        .then(handleUnauthorized)
        .then(res => {
            hideSpinner();
            if(res && res.statusCode === 2000) {
                loadCarList();
                showAlert('Xóa xe thành công!','success');
            } else {
                showAlert(res?.message || 'Xóa xe thất bại!','danger');
            }
        })
        .catch(()=>{ hideSpinner(); showAlert('Lỗi kết nối máy chủ!','danger'); });
    }
}

function loadCarChart() {
    document.getElementById("pageTitle").innerText = "Thống kê xe ô tô";
    document.getElementById("mainContent").innerHTML = `
        <div class="chart-container">
            <h5>Thống kê số lượng xe theo khoảng giá</h5>
            <div class="mb-2 d-flex align-items-center">
                <select id="carChartStyleSelect" class="form-select w-auto me-2" onchange="updateCarChart()">
                    <option value="bar">Cột</option>
                    <option value="pie">Tròn</option>
                    <option value="doughnut">Doughnut</option>
                    <option value="line">Đường</option>
                </select>
                <button class="btn btn-outline-success ms-2" onclick="downloadCarChartImage()">
                    <i class="bi bi-download"></i> Tải ảnh biểu đồ
                </button>
            </div>
            <canvas id="carChart" width="350" height="180"></canvas>
        </div>
    `;
    $('#carContent').hide();
    $('#mainContent').show();
    renderCarChart();
}

let carChartInstance = null;
function renderCarChart(chartStyle = "bar") {
    const ctx = document.getElementById("carChart");
    if (!ctx) return;
    showSpinner();
    fetch("/cars/statistics/by-price-range", {
        headers: { "Authorization": "Bearer " + getToken() }
    })
    .then(res => res.json())
    .then(result => {
        hideSpinner();
        if (!result || !result.data) {
            showAlert("Không có dữ liệu thống kê xe.", "warning");
            return;
        }
        const labels = result.data.map(item => item.key);
        const dataSet = result.data.map(item => item.count);
        const backgroundColors = [
            "#4e79a7", "#f28e2b", "#e15759", "#76b7b2", "#59a14f", "#edc949",
            "#af7aa1", "#ff9da7", "#9c755f", "#bab0ab", "#86b7fe", "#ffc107"
        ];
        if (carChartInstance) carChartInstance.destroy();
        carChartInstance = new Chart(ctx, {
            type: chartStyle,
            data: {
                labels: labels,
                datasets: [{
                    label: "Số lượng xe",
                    data: dataSet,
                    backgroundColor: chartStyle === "bar" || chartStyle === "line"
                        ? "rgba(54, 162, 235, 0.6)"
                        : backgroundColors,
                    borderColor: chartStyle === "bar" || chartStyle === "line"
                        ? "rgba(54, 162, 235, 1)"
                        : "#fff",
                    borderWidth: 1,
                    fill: chartStyle === "line",
                    tension: chartStyle === "line" ? 0.4 : 0
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: "Thống kê số lượng xe theo khoảng giá",
                        font: { size: 18 }
                    },
                    legend: {
                        display: chartStyle !== "bar" && chartStyle !== "line",
                        position: "bottom"
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                if (chartStyle === "pie" || chartStyle === "doughnut") {
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const value = context.parsed;
                                    const percent = ((value / total) * 100).toFixed(1);
                                    return `${context.label}: ${value} (${percent}%)`;
                                }
                                return `${context.label}: ${context.parsed.y ?? context.parsed}`;
                            }
                        }
                    }
                },
                scales: (chartStyle === "bar" || chartStyle === "line") ? {
                    x: { beginAtZero: true, grid: { display: false } },
                    y: { beginAtZero: true, precision: 0 }
                } : {}
            }
        });
    })
    .catch(() => {
        hideSpinner();
        showAlert("Lỗi kết nối máy chủ. Không thể tải dữ liệu thống kê xe.", "danger");
    });
}

function updateCarChart() {
    const chartStyle = document.getElementById("carChartStyleSelect").value;
    renderCarChart(chartStyle);
}

function downloadCarChartImage() {
    if (!carChartInstance) {
        showAlert("Chưa có biểu đồ để xuất.", "warning");
        return;
    }
    const link = document.createElement("a");
    link.href = carChartInstance.toBase64Image();
    link.download = "car_chart.png";
    link.click();
}

function openCarImportModal() {
    document.getElementById("carImportForm").reset();
    new bootstrap.Modal(document.getElementById("carImportModal")).show();
}

document.getElementById("carImportForm").addEventListener("submit", function(e) {
    e.preventDefault();
    const fileInput = document.getElementById("carImportFile");
    if (!fileInput.files.length) {
        showAlert("Vui lòng chọn file.", "warning");
        return;
    }
    const file = fileInput.files[0];
    const reader = new FileReader();
    reader.onload = function(event) {
        let cars = [];
        if (file.name.endsWith(".csv")) {
            // Parse CSV
            const text = event.target.result;
            cars = parseCSVCars(text);
        } else if (file.name.endsWith(".xlsx")) {
            // Parse Excel
            const data = new Uint8Array(event.target.result);
            const workbook = XLSX.read(data, { type: "array" });
            const sheet = workbook.Sheets[workbook.SheetNames[0]];
            cars = XLSX.utils.sheet_to_json(sheet, { defval: "" });
        } else {
            showAlert("Định dạng file không hỗ trợ.", "danger");
            return;
        }
        importCarsBatch(cars);
    };
    if (file.name.endsWith(".csv")) {
        reader.readAsText(file, "UTF-8");
    } else {
        reader.readAsArrayBuffer(file);
    }
});

function parseCSVCars(csvText) {
    const lines = csvText.split(/\r?\n/).filter(line => line.trim());
    if (lines.length < 2) return [];
    const headers = lines[0].split(",").map(h => h.trim());
    return lines.slice(1).map(line => {
        const values = line.split(",");
        const obj = {};
        headers.forEach((h, i) => obj[h] = values[i] ? values[i].trim() : "");
        return obj;
    });
}

function importCarsBatch(cars) {
    if (!Array.isArray(cars) || cars.length === 0) {
        showAlert("Không có dữ liệu hợp lệ.", "warning");
        return;
    }
    let success = 0, fail = 0;
    let done = 0;
    showSpinner();
    cars.forEach(car => {
        // Map field cho đúng backend
        const data = {
            name: car.name || car["Tên xe"] || "",
            price: car.price || car["Giá"] || 0,
            manufacturedYear: car.manufacturedYear || car["Năm sản xuất"] || "",
            state: car.state || car["Tình trạng"] || "",
            mileage: car.mileage || car["Số km đã đi"] || "",
            origin: car.origin || car["Xuất xứ"] || "",
            vehicleType: car.vehicleType || car["Loại xe"] || "",
            engine: car.engine || car["Động cơ"] || "",
            exteriorColor: car.exteriorColor || car["Màu ngoại thất"] || "",
            interiorColor: car.interiorColor || car["Màu nội thất"] || "",
            seats: car.seats || car["Số ghế"] || "",
            doors: car.doors || car["Số cửa"] || "",
            imgLink: car.imgLink || car["Link ảnh"] || "",
            description: car.description || car["Mô tả"] || ""
        };
        fetch("/cars", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + getToken()
            },
            body: JSON.stringify(data)
        })
        .then(handleUnauthorized)
        .then(res => {
            if (res && res.statusCode === 2000) {
                success++;
            } else fail++;
        })
        .catch(() => { fail++; })
        .finally(() => {
            done++;
            if (done === cars.length) finishCarImport(success, fail);
        });
    });
}

function finishCarImport(success, fail) {
    hideSpinner();
    showAlert(`Nhập thành công ${success} xe. Thất bại: ${fail}.`, success > 0 ? "success" : "danger");
    bootstrap.Modal.getInstance(document.getElementById("carImportModal")).hide();
    if (success > 0) {
        carSearchResults = null;
        renderCarTable();
    }
}

function exportCars(type) {
    showSpinner();
    // Lấy toàn bộ xe (không phân trang)
    fetchCars(0, 10000, "", carSort, carDirection)
    .then(data => {
        hideSpinner();
        if (!data || !data.data || !data.data.content) {
            showAlert("Không có dữ liệu để xuất.", "warning");
            return;
        }
        const cars = data.data.content;
        if (type === "csv") {
            exportCarsToCSV(cars);
        } else {
            exportCarsToExcel(cars);
        }
    });
}

function exportCarsToCSV(cars) {
    const headers = [
        "carId", "name", "price", "manufacturedYear", "state", "mileage", "origin", "vehicleType", "engine", "exteriorColor", "interiorColor", "seats", "doors", "imgLink", "description"
    ];
    const csvRows = [
        headers.join(","),
        ...cars.map(c => headers.map(h => `"${(c[h] || "").toString().replace(/"/g, '""')}"`).join(","))
    ];
    const blob = new Blob([csvRows.join("\r\n")], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "cars.csv";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

function exportCarsToExcel(cars) {
    const headers = [
        "carId", "name", "price", "manufacturedYear", "state", "mileage", "origin", "vehicleType", "engine", "exteriorColor", "interiorColor", "seats", "doors", "imgLink", "description"
    ];
    const data = cars.map(c => {
        const obj = {};
        headers.forEach(h => obj[h] = c[h] || "");
        return obj;
    });
    const ws = XLSX.utils.json_to_sheet(data, { header: headers });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Cars");
    const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
    const blob = new Blob([wbout], { type: "application/octet-stream" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "cars.xlsx";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

// Sidebar menu logic chuyên nghiệp
function closeAllMenus() {
    document.querySelectorAll('.sidebar-collapse').forEach(div => div.classList.remove('open'));
    document.querySelectorAll('.chevron').forEach(ic => ic.classList.remove('open'));
    document.querySelectorAll('.sidebar-parent').forEach(parent => parent.classList.remove('active'));
}
function setActiveMenu(menuId) {
    // Xóa active khỏi tất cả menu con và link đơn
    document.querySelectorAll('.sidebar-child, .sidebar-link').forEach(el => el.classList.remove('active'));
    // Đánh dấu active cho menu con hoặc link đơn
    const activeItem = document.getElementById(menuId);
    if (activeItem) {
        activeItem.classList.add('active');
        // Nếu là menu con, mở menu cha và xoay icon
        if (activeItem.classList.contains('sidebar-child')) {
            const parentCollapse = activeItem.closest('.sidebar-collapse');
            if (parentCollapse) {
                parentCollapse.classList.add('open');
                const parentLink = parentCollapse.previousElementSibling;
                if (parentLink && parentLink.classList.contains('sidebar-parent')) {
                    parentLink.classList.add('active');
                    const icon = parentLink.querySelector('.chevron');
                    if (icon) icon.classList.add('open');
                }
            }
        }
    }
    // Đảm bảo chỉ menu cha chứa menu con active được mở
    document.querySelectorAll('.sidebar-group').forEach(group => {
        const collapse = group.querySelector('.sidebar-collapse');
        const parent = group.querySelector('.sidebar-parent');
        const chevron = group.querySelector('.chevron');
        if (collapse && parent && chevron) {
            if (!collapse.contains(document.querySelector('.sidebar-child.active'))) {
                collapse.classList.remove('open');
                parent.classList.remove('active');
                chevron.classList.remove('open');
            }
        }
    });
}
function setupSidebarMenu() {
    // Menu cha toggle
    document.querySelectorAll('.sidebar-parent').forEach(parent => {
        parent.addEventListener('click', function(e) {
            e.preventDefault();
            const collapse = parent.nextElementSibling;
            const chevron = parent.querySelector('.chevron');
            const isOpen = collapse.classList.contains('open');
            // Đóng tất cả menu cha khác
            document.querySelectorAll('.sidebar-collapse').forEach(div => {
                if (div !== collapse) div.classList.remove('open');
            });
            document.querySelectorAll('.chevron').forEach(ic => {
                if (ic !== chevron) ic.classList.remove('open');
            });
            document.querySelectorAll('.sidebar-parent').forEach(p => {
                if (p !== parent) p.classList.remove('active');
            });
            // Toggle menu này
            if (isOpen) {
                collapse.classList.remove('open');
                chevron.classList.remove('open');
                parent.classList.remove('active');
            } else {
                collapse.classList.add('open');
                chevron.classList.add('open');
                parent.classList.add('active');
            }
        });
    });
    // Menu con: set active và gọi hàm load
    document.getElementById('menu-user-list').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-user-list');
        loadUserList();
    };
    document.getElementById('menu-user-stats').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-user-stats');
        loadUserChart();
    };
    document.getElementById('menu-car-list').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-car-list');
        loadCarList();
    };
    document.getElementById('menu-car-stats').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-car-stats');
        loadCarChart();
    };
    document.getElementById('menu-welcome').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-welcome');
        loadWelcome();
    };
    document.getElementById('menu-logout').onclick = function(e) {
        e.preventDefault();
        logout();
    };
    document.getElementById('menu-order-list').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-order-list');
        loadOrderList();
    };
    document.getElementById('menu-order-stats').onclick = function(e) {
        e.preventDefault();
        setActiveMenu('menu-order-stats');
        loadOrderStats();
    };
}
document.addEventListener('DOMContentLoaded', function() {
    setupSidebarMenu();
    setActiveMenu('menu-welcome');
});
// ================== Quản lý ĐƠN HÀNG ==================
let currentOrderPage = 0;
let orderStatusFilter = "";
let orderSearch = "";
let orderSort = "orderDate";
let orderDirection = "desc";
function loadOrderList() {
    document.getElementById("pageTitle").innerText = "Quản lý đơn hàng";
    $('#carContent').hide();
    $('#mainContent').show();
    renderOrderTable();
}

function renderOrderTable() {
    const params = new URLSearchParams({
        page: currentOrderPage,
        size: itemsPerPage,
        sortBy: orderSort,
        direction: orderDirection
    });
    if (orderSearch) {
        params.append("keyword", orderSearch);
    }
    const url = orderSearch
        ? `/orders/search?${params.toString()}`
        : `/orders?${params.toString()}`;
    showSpinner();
    fetch(url, {
        headers: {
            "Authorization": "Bearer " + getToken()
        }
    })

        .then(handleUnauthorized)
        .then(data => {
            hideSpinner();
            // Kiểm tra xem data có đúng cấu trúc hay không:
            if (!data || !data.content) {
                showAlert("Không thể tải dữ liệu đơn hàng.", "danger");
                return;
            }

            // Lấy mảng đơn hàng và tổng số trang
            const orders = data.content;
            const totalPages = data.totalPages;

            // Tạo HTML hiển thị bảng
            let html = `
<div class="mb-3 d-flex justify-content-between align-items-center">
  <div class="d-flex">
     <input type="text" id="orderSearchInput" class="form-control me-2"
           placeholder="Tìm theo tên khách hàng..." value="" oninput="toggleClearSearchBtn()">
    <button class="btn btn-primary me-2" onclick="searchOrders()">
      <i class="bi bi-search"></i>
    </button>
    <button class="btn btn-secondary" id="clearSearchBtn" style="display: none" onclick="clearOrderSearch()">
      <i class="bi bi-x"></i>
    </button>
     <button class="btn btn-success text-white d-flex align-items-center gap-2" onClick="openOrderModal()">
            <i class="bi bi-plus-circle-fill fs-5"></i>
  
    </button>
  </div>
  <div>
    <button class="btn btn-outline-primary me-2" onclick="openImportOrderModal()">
      <i class="bi bi-upload"></i> Nhập file
    </button>
    <button class="btn btn-outline-secondary me-2" onclick="exportOrders('csv')">
      <i class="bi bi-download"></i> Xuất CSV
    </button>
    <button class="btn btn-outline-success" onclick="exportOrders('excel')">
      <i class="bi bi-file-earmark-excel"></i> Xuất Excel
    </button>
  </div>
</div>
<div class="mb-2 d-flex align-items-center">
  <label class="me-2 mb-0">Sắp xếp theo:</label>
  <select onchange="changeOrderSort(this.value)" class="form-select w-auto me-3">
    <option value="orderDate" ${orderSort === 'orderDate' ? 'selected' : ''}>Ngày đặt</option>
    <option value="customerName" ${orderSort === 'customerName' ? 'selected' : ''}>Khách hàng</option>
    <option value="status" ${orderSort === 'status' ? 'selected' : ''}>Trạng thái</option>
  </select>
  <select onchange="changeOrderDirection(this.value)" class="form-select w-auto">
    <option value="asc" ${orderDirection === 'asc' ? 'selected' : ''}>Tăng dần</option>
    <option value="desc" ${orderDirection === 'desc' ? 'selected' : ''}>Giảm dần</option>
  </select>
</div>
<table class="table table-bordered"><thead><tr>
  <th>Khách hàng</th>
  <th>SĐT</th>
  <th>Ngày đặt</th>
  <th>Mẫu xe</th>
  <th>Số lượng</th>
  <th>Giá</th>
  <th>Trạng thái</th>
  <th>Nơi mua</th>
  <th>Hành động</th>
</tr></thead><tbody>`;

            orders.forEach(o => {
                html += `<tr>
              <td>${o.customerName || ""}</td>
              <td>${o.phoneNumber || ""}</td>
              <td>${o.orderDate || ""}</td>
              <td>${o.carModel || ""}</td>
              <td>${o.quantity}</td>
              <td>${o.price?.toLocaleString() || ""}</td>
              <td>${o.status || ""}</td>
              <td>${o.placeOfPurchase || ""}</td>
              <td>
                <button class="btn btn-warning btn-sm" onclick="editOrder('${o.id}')">
                  <i class="bi bi-pencil-fill"></i>
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteOrder('${o.id}')">
                  <i class="bi bi-trash-fill"></i>
                </button>
              </td>
            </tr>`;
            });

            html += `</tbody></table>` + renderOrderPagination(totalPages);
            document.getElementById("mainContent").innerHTML = html;
        })
        .catch(() => {
            hideSpinner();
            showAlert("Không thể tải dữ liệu đơn hàng.", "danger");
        });

}

function renderOrderPagination(totalPages) {
    if (totalPages <= 1) return "";

    let html = '<nav><ul class="pagination justify-content-center">';

    // Nút "First" («)
    html += `<li class="page-item ${currentOrderPage === 0 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="goToOrderPage(0)" title="Trang đầu">&laquo;</a>
             </li>`;

    // Nếu currentOrderPage > 2, hiển thị "1" và "…"
    if (currentOrderPage > 2) {
        html += `<li class="page-item"><a class="page-link" href="#" onclick="goToOrderPage(0)">1</a></li>`;
        html += `<li class="page-item disabled"><span class="page-link">…</span></li>`;
    }

    // Hiện 2 trang trước, hiện tại, 2 trang sau
    const start = Math.max(0, currentOrderPage - 2);
    const end = Math.min(totalPages - 1, currentOrderPage + 2);
    for (let i = start; i <= end; i++) {
        html += `<li class="page-item ${i === currentOrderPage ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="goToOrderPage(${i})">${i + 1}</a>
                 </li>`;
    }

    // Nếu currentOrderPage < totalPages-3, hiển thị "…" và cuối cùng
    if (currentOrderPage < totalPages - 3) {
        html += `<li class="page-item disabled"><span class="page-link">…</span></li>`;
        html += `<li class="page-item"><a class="page-link" href="#" onclick="goToOrderPage(${totalPages - 1})">${totalPages}</a></li>`;
    }

    // Nút "Last" (»)
    html += `<li class="page-item ${currentOrderPage === totalPages - 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="goToOrderPage(${totalPages - 1})" title="Trang cuối">&raquo;</a>
             </li>`;

    html += '</ul></nav>';
    return html;
}

function goToOrderPage(page) {
    currentOrderPage = page;
    renderOrderTable();
}
function searchOrders() {
    const input = document.getElementById("orderSearchInput").value.trim();
    orderSearch = input;
    currentOrderPage = 0;
    renderOrderTable();
}

function clearOrderSearch() {
    document.getElementById("orderSearchInput").value = "";
    orderSearch = "";
    toggleClearSearchBtn();
    currentOrderPage = 0;
    renderOrderTable();
}
function toggleClearSearchBtn() {
    const input = document.getElementById("orderSearchInput").value.trim();
    const btn = document.getElementById("clearSearchBtn");
    btn.style.display = input.length > 0 ? "inline-block" : "none";
}
function changeOrderSort(sortBy) {
    orderSort = sortBy;
    currentOrderPage = 0;
    renderOrderTable();
}

function changeOrderDirection(direction) {
    orderDirection = direction;
    currentOrderPage = 0;
    renderOrderTable();
}

// ================== CRUD ĐƠN HÀNG ==================

function openOrderModal() {

    loadCustomersAndCars();
    new bootstrap.Modal(document.getElementById("orderModal")).show();
}

function loadCustomersAndCars() {
    fetch("/users", {
        headers: {"Authorization": "Bearer " + getToken()}
    })
        .then(res => res.json())
        .then(users => {
            const customerSelect = document.getElementById("customerSelect");
            customerSelect.innerHTML = "";
            users.forEach(u => {
                const fullName = (u.lastName ? u.lastName : "") + (u.firstName ? (" " + u.firstName) : "");
                customerSelect.innerHTML += `<option value="${u.id}">${fullName.trim()}</option>`;
            });
        });

    fetch("/cars", {
        headers: {"Authorization": "Bearer " + getToken()}
    })
        .then(res => {
            if (!res.ok) throw new Error(`Lỗi HTTP! Trạng thái: ${res.status}`);
            return res.json();
        })
        .then(data => {
            const carSelect = document.getElementById("carSelect");
            carSelect.innerHTML = "";
            const cars = Array.isArray(data) ? data : data.data || [];
            if (cars.length === 0) {
                carSelect.innerHTML = '<option value="">Không có xe</option>';
                return;
            }
            cars.forEach(c => {
                carSelect.innerHTML += `<option value="${c.carId}">${c.name}</option>`;
            });
        })
        .catch(error => {
            document.getElementById("carSelect").innerHTML = '<option value="">Lỗi khi tải xe</option>';
        });
}

function submitOrder() {
    const order = {
        userId: document.getElementById("customerSelect").value,
        carId: parseInt(document.getElementById("carSelect").value),
        orderDate: document.getElementById("orderDate").value,
        quantity: parseInt(document.getElementById("quantity").value),
        price: parseFloat(document.getElementById("price").value),
        status: document.getElementById("status").value,
        placeOfPurchase: document.getElementById("placeOfPurchase").value
    };

    fetch("/orders", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getToken()
        },
        body: JSON.stringify(order)
    })
        .then(res => {
            if (!res.ok) throw new Error("Lỗi khi tạo đơn hàng");
            return res.json();
        })
        .then(() => {
            showAlert("Tạo đơn hàng thành công!", "success");

            const modalElement = document.getElementById("orderModal");
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) modalInstance.hide();

            loadOrderList(); // reload lại bảng danh sách
        })
        .catch(err => {
            showAlert("Tạo đơn hàng thất bại: " + err.message, "danger");
        });
}

function editOrder(orderId) {
    fetch(`/orders/${orderId}`, {
        headers: {"Authorization": "Bearer " + getToken()}
    })
        .then(res => res.json())
        .then(order => {
            document.getElementById("editOrderId").value = order.id;
            document.getElementById("editCustomerName").value = order.customerName;
            document.getElementById("editOrderDate").value = order.orderDate;
            document.getElementById("editQuantity").value = order.quantity;
            document.getElementById("editPrice").value = order.price;
            document.getElementById("editStatus").value = order.status;
            document.getElementById("editPlaceOfPurchase").value = order.placeOfPurchase;

            // load danh sách xe vào dropdown
            fetch("/cars", {
                headers: {"Authorization": "Bearer " + getToken()}
            })
                .then(res => {
                    if (!res.ok) throw new Error(`Lỗi HTTP! Trạng thái: ${res.status}`);
                    return res.json();
                })
                .then(data => {
                    console.log('Phản hồi xe:', data); // Kiểm tra phản hồi
                    const select = document.getElementById("editCarSelect");
                    select.innerHTML = "";
                    const cars = Array.isArray(data) ? data : data.data || [];
                    if (cars.length === 0) {
                        console.warn('Không tìm thấy xe');
                        select.innerHTML = '<option value="">Không có xe</option>';
                        return;
                    }
                    cars.forEach(c => {
                        select.innerHTML += `<option value="${c.carId}" ${c.name === order?.carModel ? "selected" : ""}>${c.name}</option>`;
                    });
                })
                .catch(error => {
                    console.error('Lỗi khi lấy xe:', error);
                    const select = document.getElementById("editCarSelect");
                    select.innerHTML = '<option value="">Lỗi khi tải xe</option>';
                });

            new bootstrap.Modal(document.getElementById("orderEditModal")).show();
        });
}

function submitOrderUpdate() {
    const id = document.getElementById("editOrderId").value;
    const data = {
        carId: parseInt(document.getElementById("editCarSelect").value),
        orderDate: document.getElementById("editOrderDate").value,
        quantity: parseInt(document.getElementById("editQuantity").value),
        price: parseFloat(document.getElementById("editPrice").value),
        status: document.getElementById("editStatus").value,
        placeOfPurchase: document.getElementById("editPlaceOfPurchase").value
    };

    fetch(`/orders/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + getToken()
        },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) throw new Error("Lỗi khi cập nhật đơn hàng");
            return res.json();
        })
        .then(() => {
            showAlert("Cập nhật đơn hàng thành công!", "success");
            bootstrap.Modal.getInstance(document.getElementById("orderEditModal")).hide();
            loadOrderList();
        })
        .catch(err => showAlert("Cập nhật đơn hàng thất bại: " + err.message, "danger"));
}

function deleteOrder(id) {
    if (!confirm("Xác nhận xóa đơn hàng?")) return;
    const token = getToken();
    if (!token) return redirectToLogin();

    fetch(`/orders/${id}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
    })
        .then(handleUnauthorized)
        .then(() => {
            showAlert("Xóa đơn hàng thành công!", "success");
            renderOrderTable();
        })
        .catch(() => showAlert("Không thể xóa đơn hàng.", "danger"));
}
function openImportOrderModal() {
    const modal = new bootstrap.Modal(document.getElementById("importOrderModal"));
    modal.show();
}

function importOrderFile() {
    const input = document.getElementById("importFileo");
    if (input.files.length === 0) {
        showAlert("Vui lòng chọn file!", "warning");
        return;
    }

    const formData = new FormData();
    formData.append("file", input.files[0]);

    fetch("/orders/import", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getToken()
        },
        body: formData
    })
        .then(res => {
            if (!res.ok) throw new Error("Import thất bại");
            return res.json();
        })
        .then(() => {
            showAlert("Import thành công!", "success");
            loadOrderList(); // hoặc reload lại
        })
        .catch(err => {
            showAlert("Lỗi khi import: " + err.message, "danger");
        });
}

function exportOrders(type) {
    const fileName = type === "csv" ? "orders.csv" : "orders.xlsx";
    fetch(`/orders/export/${type}`, {
        headers: {
            "Authorization": "Bearer " + getToken()
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("Xuất file thất bại");
            return res.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            a.click();
            window.URL.revokeObjectURL(url);
        })
        .catch(err => showAlert("Lỗi khi xuất file: " + err.message, "danger"));
}
function loadOrderStats() {
    document.getElementById("pageTitle").innerText = "Thống kê đơn hàng";
    $('#carContent').hide();
    $('#mainContent').show();
    document.getElementById("mainContent").innerHTML = `
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5>Biểu đồ thống kê đơn hàng</h5>
        <div>
          <select class="form-select d-inline-block w-auto me-2" id="orderStatType" onchange="updateOrderChart()">
            <option value="status">Theo trạng thái</option>
            <option value="price">Theo khoảng giá trị</option>
          </select>
          <select class="form-select d-inline-block w-auto me-2" id="orderChartType" onchange="updateOrderChart()">
            <option value="bar">Biểu đồ cột</option>
            <option value="pie">Biểu đồ tròn</option>
            <option value="line">Biểu đồ đường</option>
            <option value="doughnut">Doughnut</option>
          </select>
          <button class="btn btn-outline-success" onclick="downloadOrderChartImage()">
            <i class="bi bi-download"></i> Tải ảnh
          </button>
        </div>
      </div>
      <canvas id="orderChart" style="height: 400px;"></canvas>
    `;
    renderOrderChart();
}
let orderChartInstance;

function renderOrderChart() {
    const token = getToken();
    const chartType = document.getElementById("orderChartType").value;
    const statType = document.getElementById("orderStatType")?.value || "status";
    let url = "/orders/statistics/by-status";
    let label = "Số đơn hàng";
    if (statType === "price") {
        url = "/orders/statistics/by-price-range";
        label = "Số đơn hàng theo khoảng giá trị";
    }
    fetch(url, {
        headers: { "Authorization": `Bearer ${token}` }
    })
        .then(res => res.json())
        .then(result => {
            const data = result.data || [];
            const labels = data.map(item => item.key);
            const values = data.map(item => item.count);
            const ctx = document.getElementById("orderChart").getContext("2d");
            if (orderChartInstance) orderChartInstance.destroy();
            orderChartInstance = new Chart(ctx, {
                type: chartType,
                data: {
                    labels,
                    datasets: [{
                        label,
                        data: values,
                        backgroundColor: [
                            "#007bff", "#28a745", "#ffc107", "#dc3545", "#6f42c1", "#4e79a7", "#f28e2b", "#e15759", "#76b7b2", "#59a14f", "#edc949"
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    scales: chartType === 'bar' || chartType === 'line' ? {
                        y: { beginAtZero: true }
                    } : {}
                }
            });
        })
        .catch(() => showAlert("Không thể tải thống kê đơn hàng.", "danger"));
}

function updateOrderChart() {
    renderOrderChart();
}
function downloadOrderChartImage() {
    const link = document.createElement("a");
    link.href = orderChartInstance.toBase64Image();
    link.download = "order-chart.png";
    link.click();
}