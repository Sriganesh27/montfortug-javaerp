$c = Get-Content -Path 'd:\Java\montforterp\ERP-Java\backend\src\main\resources\static\dashboard.html'
$n = $c[0..227] + '<div id="main-content"></div>' + $c[517..546]
Set-Content -Path 'd:\Java\montforterp\ERP-Java\backend\src\main\resources\static\views\admin\dashboard.html' -Value $n
