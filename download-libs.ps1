# Create the js directory if it doesn't exist
$jsDir = "mind-flow-main/src/main/resources/static/js"
New-Item -ItemType Directory -Force -Path $jsDir

# Download jQuery
Invoke-WebRequest -Uri "https://code.jquery.com/jquery-3.6.0.min.js" -OutFile "$jsDir/jquery-3.6.0.min.js"

# Download Chart.js
Invoke-WebRequest -Uri "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.7.0/chart.min.js" -OutFile "$jsDir/chart.min.js"

# Download Popper.js
Invoke-WebRequest -Uri "https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js" -OutFile "$jsDir/popper.min.js"

# Download Bootstrap
Invoke-WebRequest -Uri "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js" -OutFile "$jsDir/bootstrap.min.js"

Write-Host "All JavaScript libraries have been downloaded successfully!" 