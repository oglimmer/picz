#!/usr/bin/env bash

set -eu

cd "$(dirname "$0")"/..

# Display usage help
show_usage() {
    echo "Usage: $(basename "$0") [option] [--restart]"
    echo ""
    echo "Options:"
    echo "  all    Build and push both frontend and backend images"
    echo "  fe     Build and push only the frontend image"
    echo "  be     Build and push only the backend image"
    echo "  help   Display this help message"
    echo ""
    echo "Parameters:"
    echo "  --restart   Restart the corresponding Kubernetes deployments after build"
    echo ""
    exit 1
}

# Parse arguments
if [ $# -lt 1 ] || [ $# -gt 2 ]; then
    echo "Error: Invalid number of arguments"
    show_usage
fi

BUILD_OPTION=$1
RESTART_FLAG=false

if [ $# -eq 2 ]; then
    if [ "$2" == "--restart" ]; then
        RESTART_FLAG=true
    else
        echo "Error: Unknown parameter '$2'"
        show_usage
    fi
fi

# Process the build command
case "$BUILD_OPTION" in
    all)
        echo "Building both frontend and backend..."
        docker build --tag registry.oglimmer.com/picz-fe --push frontend -f frontend/Dockerfile-prod
        docker build --tag registry.oglimmer.com/picz-be --push . -f backend/Dockerfile
        echo "✅ All builds completed successfully!"

        if [ "$RESTART_FLAG" == "true" ]; then
            echo "Restarting Kubernetes deployments..."
            kubectl rollout restart deployment/picz-web
            kubectl rollout restart deployment/picz-api
            echo "✅ Deployments restarted successfully!"
        fi
        ;;
    fe)
        echo "Building frontend only..."
        docker build --tag registry.oglimmer.com/picz-fe --push frontend -f frontend/Dockerfile-prod
        echo "✅ Frontend build completed successfully!"

        if [ "$RESTART_FLAG" == "true" ]; then
            echo "Restarting frontend Kubernetes deployment..."
            kubectl rollout restart deployment/picz-web
            echo "✅ Frontend deployment restarted successfully!"
        fi
        ;;
    be)
        echo "Building backend only..."
        docker build --tag registry.oglimmer.com/picz-be --push . -f backend/Dockerfile
        echo "✅ Backend build completed successfully!"

        if [ "$RESTART_FLAG" == "true" ]; then
            echo "Restarting backend Kubernetes deployment..."
            kubectl rollout restart deployment/picz-api
            echo "✅ Backend deployment restarted successfully!"
        fi
        ;;
    help)
        show_usage
        ;;
    *)
        echo "Error: Invalid option '$BUILD_OPTION'"
        show_usage
        ;;
esac
