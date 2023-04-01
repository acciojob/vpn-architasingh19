package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (user.getMaskedIp() != null) throw new Exception("Already connected");
        if (countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())) return user;
        if (user.getServiceProviderList() == null) {
            throw new Exception("Unable to connect");
        }
        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        int a = Integer.MAX_VALUE;
        ServiceProvider serviceProvider = null;
        Country country = null;
        for (ServiceProvider serviceProvider1 : serviceProviderList) {
            List<Country> countryList = serviceProvider1.getCountryList();
            for (Country country1 : countryList) {
                if (countryName.equalsIgnoreCase(country1.getCountryName().toString()) && a > serviceProvider1.getId()) {
                    a = serviceProvider1.getId();
                    serviceProvider = serviceProvider1;
                    country = country1;
                }
            }
        }
        if (serviceProvider != null) {
            Connection connection = new Connection();
            connection.setUser(user);
            connection.setServiceProvider(serviceProvider);
            user.setMaskedIp(country.getCode() + "." + serviceProvider.getId() + "." + userId);
            user.setConnected(true);
            user.getConnectionList().add(connection);
            serviceProvider.getConnectionList().add(connection);
            userRepository2.save(user);
            serviceProviderRepository2.save(serviceProvider);
        }
        else throw new Exception("Unable to connect");
        return user;
    }

    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (user.getConnected() == false) {
            throw new Exception("Already disconnected");
        }
        user.setMaskedIp(null);
        user.setConnected(false);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User user = userRepository2.findById(senderId).get();
        User user1 = userRepository2.findById(receiverId).get();
        if (user1.getMaskedIp() != null) {
            String str = user1.getMaskedIp();
            String code = str.substring(0, 3);
            code = code.toUpperCase();
            if (code.equals(user.getOriginalCountry().getCode())) return user;
            String countryName = "";
            CountryName[] countryNames = CountryName.values();
            for (CountryName countryName1 : countryNames)
                if (countryName1.toCode().toString().equals(code)) countryName = countryName1.toString();
            try {
                user = connect(senderId, countryName);
            } catch (Exception e) {
                throw new Exception("Cannot establish communication");
            }
            if (!user.getConnected()) throw new Exception("Cannot establish communication");
            return user;
        }
        if (user1.getOriginalCountry().equals(user.getOriginalCountry())) return user;
        String countryName = user1.getOriginalCountry().getCountryName().toString();
        try {
            user = connect(senderId, countryName);
        } catch (Exception e) {
            if (!user.getConnected()) throw new Exception("Cannot establish communication");
        }
        return user;
    }
}