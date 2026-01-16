package ru.services.db;

import ru.data.DAO.Syst;

import java.util.Set;

public interface IRandomizeService {
    Set<Long> getSchools();

    Syst getSyst();

    void setSyst(Syst syst);

    void createRandomData();

    void removeRandomData();
}
