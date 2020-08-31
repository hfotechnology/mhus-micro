package de.mhus.micro.oper.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.mhus.lib.core.operation.Operation;

public interface OperationsAdmin {

    void list(List<Operation> results);
    
    default List<Operation> list() {
        ArrayList<Operation> results = new ArrayList<>();
        list(results);
        return results;
    }

    Operation getOperation(String path, String version);

    Operation getOperation(UUID uuid);
    
}
