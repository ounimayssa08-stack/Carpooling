package com.carpooling.controller;

/**
 * Implemented by sub-view controllers that need a reference to the DashboardController
 * for in-app navigation.
 */
public interface DashboardAware {
    void setDashboardController(DashboardController dashboardController);
}
