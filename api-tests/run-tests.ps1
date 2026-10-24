$base = "http://localhost:8080"
$script:pass = 0
$script:fail = 0

function Sql($query) {
    docker exec cafe-mysql mysql -uroot -prootpass cafe_db -e $query 2>$null | Out-Null
}

function Call($method, $path, $token, $body) {
    $p = @{ Uri = "$base$path"; Method = $method; UseBasicParsing = $true }
    if ($token) { $p.Headers = @{ Authorization = "Bearer $token" } }
    if ($body)  { $p.Body = $body; $p.ContentType = "application/json" }
    try {
        $r = Invoke-WebRequest @p
        return @{ Status = [int]$r.StatusCode; Body = $r.Content }
    } catch {
        $status = 0
        if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
        $text = ""
        if ($_.ErrorDetails) { $text = $_.ErrorDetails.Message }
        return @{ Status = $status; Body = $text }
    }
}

function Json($r) { if ($r.Body) { $r.Body | ConvertFrom-Json } }

function Check($name, $expected, $r) {
    if ($r.Status -eq $expected) { Write-Host "PASS  $name -> $($r.Status)" -ForegroundColor Green; $script:pass++ }
    else { Write-Host "FAIL  $name -> expected $expected, got $($r.Status)  $($r.Body)" -ForegroundColor Red; $script:fail++ }
}

function Assert($name, $cond) {
    if ($cond) { Write-Host "PASS  $name" -ForegroundColor Green; $script:pass++ }
    else { Write-Host "FAIL  $name" -ForegroundColor Red; $script:fail++ }
}

# ---------- setup ----------
Write-Host "== Setup =="
Sql "DELETE FROM order_items; DELETE FROM orders; DELETE FROM reservations;"
Call POST "/auth/register" $null '{"username":"satvik","password":"pass123"}' | Out-Null
Call POST "/auth/register" $null '{"username":"customer","password":"pass123"}' | Out-Null
Sql "UPDATE users SET role='ADMIN' WHERE username='satvik';"
$admin = (Json (Call POST "/auth/login" $null '{"username":"satvik","password":"pass123"}')).token
$cust  = (Json (Call POST "/auth/login" $null '{"username":"customer","password":"pass123"}')).token
if (-not $admin -or -not $cust) { Write-Host "Login failed. Is the app running?" -ForegroundColor Red; exit 1 }

# ---------- menu ----------
Write-Host "== Menu =="
Check "GET /menu (public)" 200 (Call GET "/menu" $null $null)
Check "GET /menu?category=PIZZA" 400 (Call GET "/menu?category=PIZZA" $null $null)
Check "POST /admin/menu as customer" 403 (Call POST "/admin/menu" $cust '{"name":"T","price":1,"category":"TEA"}')

# ---------- reservations ----------
Write-Host "== Reservations =="
$d = (Get-Date).AddDays(30).ToString("yyyy-MM-dd")
function Res($s, $e, $size, $table = 1, $date = $d) {
    @{ tableId = $table; date = $date; startTime = $s; endTime = $e; partySize = $size } | ConvertTo-Json -Compress
}
$r1 = Call POST "/reservations" $cust (Res "18:00" "19:00" 2)
Check "R1 book 18-19" 201 $r1
$rid = (Json $r1).id
Check "R2 overlap" 409 (Call POST "/reservations" $cust (Res "18:30" "19:30" 2))
Check "R3 back-to-back" 201 (Call POST "/reservations" $cust (Res "19:00" "20:00" 2))
Check "R4 party too big" 400 (Call POST "/reservations" $cust (Res "20:00" "21:00" 3))
Check "R5 end before start" 400 (Call POST "/reservations" $cust (Res "19:00" "18:00" 2))
Check "R6 past date" 400 (Call POST "/reservations" $cust (Res "18:00" "19:00" 2 1 "2020-01-01"))
Check "R7 unknown table" 404 (Call POST "/reservations" $cust (Res "18:00" "19:00" 2 999))
Check "R8 no token" 403 (Call POST "/reservations" $null (Res "18:00" "19:00" 2))
Check "R9 list mine" 200 (Call GET "/reservations/my" $cust $null)
Check "R10 cancel as non-owner" 403 (Call DELETE "/reservations/$rid" $admin $null)
Check "R11 cancel as owner" 200 (Call DELETE "/reservations/$rid" $cust $null)
Check "R12 rebook freed slot" 201 (Call POST "/reservations" $cust (Res "18:00" "19:00" 2))

# ---------- orders ----------
Write-Host "== Orders =="
$o1 = '{"items":[{"menuItemId":1,"quantity":2},{"menuItemId":3,"quantity":1}]}'
$r = Call POST "/orders" $cust $o1
Check "O1 place order" 201 $r
Assert "O1 total is 300" ((Json $r).totalAmount -eq 300)
Check "O2 missing item" 404 (Call POST "/orders" $cust '{"items":[{"menuItemId":1,"quantity":1},{"menuItemId":9999,"quantity":1}]}')
Check "O3 empty order" 400 (Call POST "/orders" $cust '{"items":[]}')
Check "O4 quantity 0" 400 (Call POST "/orders" $cust '{"items":[{"menuItemId":1,"quantity":0}]}')
Check "O5 no token" 403 (Call POST "/orders" $null $o1)
Check "O6 list mine" 200 (Call GET "/orders/my" $cust $null)
Sql "UPDATE menu_items SET available=0 WHERE id=6;"
Check "O7 unavailable item" 409 (Call POST "/orders" $cust '{"items":[{"menuItemId":6,"quantity":1}]}')
Sql "UPDATE menu_items SET available=1 WHERE id=6;"

# ---------- lifecycle (fails until you build it) ----------
Write-Host "== Order lifecycle =="
function St($s) { @{ status = $s } | ConvertTo-Json -Compress }
$oid = (Json (Call POST "/orders" $cust $o1)).id
Check "L1 PLACED->PREPARING" 200 (Call PATCH "/admin/orders/$oid/status" $admin (St "PREPARING"))
Check "L2 PREPARING->PLACED" 409 (Call PATCH "/admin/orders/$oid/status" $admin (St "PLACED"))
Check "L3 repeat PREPARING" 409 (Call PATCH "/admin/orders/$oid/status" $admin (St "PREPARING"))
Check "L4 PREPARING->SERVED" 200 (Call PATCH "/admin/orders/$oid/status" $admin (St "SERVED"))
Check "L5 SERVED->CANCELLED" 409 (Call PATCH "/admin/orders/$oid/status" $admin (St "CANCELLED"))
Check "L6 customer PATCH" 403 (Call PATCH "/admin/orders/$oid/status" $cust (St "PREPARING"))
Check "L7 bad status" 400 (Call PATCH "/admin/orders/$oid/status" $admin '{"status":"PIZZA"}')
Check "L8 missing status" 400 (Call PATCH "/admin/orders/$oid/status" $admin '{}')
Check "L9 unknown order" 404 (Call PATCH "/admin/orders/9999/status" $admin (St "PREPARING"))

# ---------- cancel ----------
Write-Host "== Cancel =="
$c1 = (Json (Call POST "/orders" $cust $o1)).id
Check "C1 cancel own PLACED" 200 (Call POST "/orders/$c1/cancel" $cust $null)
Check "C2 cancel again" 409 (Call POST "/orders/$c1/cancel" $cust $null)
$c3 = (Json (Call POST "/orders" $cust $o1)).id
Call PATCH "/admin/orders/$c3/status" $admin (St "PREPARING") | Out-Null
Check "C3 cancel after PREPARING" 409 (Call POST "/orders/$c3/cancel" $cust $null)
$c4 = (Json (Call POST "/orders" $admin $o1)).id
Check "C4 cancel someone else's order" 404 (Call POST "/orders/$c4/cancel" $cust $null)
Check "C5 cancel unknown order" 404 (Call POST "/orders/9999/cancel" $cust $null)

Write-Host ""
Write-Host "Passed: $($script:pass)   Failed: $($script:fail)"
if ($script:fail -gt 0) { exit 1 }