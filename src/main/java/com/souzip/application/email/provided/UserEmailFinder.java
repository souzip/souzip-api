package com.souzip.application.email.provided;

import java.util.List;

public interface UserEmailFinder {

    List<String> findDistinctEmailsForActiveUsers();
}
