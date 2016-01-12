package com.focusit.jsflight.player.controller;

/**
 * UI Controller interface.
 * Any controller must have two main methods to store and load content from a file in certain directory 
 * @author dkirpichenkov
 *
 */
public interface IUIController
{
    final String defaultConfig = "params";

    /**
     * load controllers data
     * @param file path to directory to store data
     */
    void load(String file) throws Exception;

    /**
     * store controller data
     * @param file path to directory to store data
     */
    void store(String file) throws Exception;
}
