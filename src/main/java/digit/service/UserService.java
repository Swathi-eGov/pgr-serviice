package digit.service;

import digit.config.Configuration;
import digit.util.UserUtil;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.CreateUserRequest;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.contract.user.UserSearchRequest;
import org.egov.common.contract.user.enums.UserType;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.Collections;
import static digit.config.ServiceConstants.CITIZEN_UPPER;

@Service
public class UserService {

    private final UserUtil userUtil;
    private final Configuration config;

    public UserService(UserUtil userUtil, Configuration config) {
        this.userUtil = userUtil;
        this.config = config;
    }

    public void callUserService(ServiceRequest request){

        if(!StringUtils.isEmpty(request.getPgrEntity().getService().getAccountId()))
            enrichUser(request);
        else if(request.getPgrEntity().getService().getCitizen()!=null)
            upsertUser(request);

    }

    private User createUser(RequestInfo requestInfo,String tenantId, User userInfo) {

        userUtil.addUserDefaultFields(userInfo.getMobileNumber(),tenantId, userInfo, CITIZEN_UPPER );
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());


        UserDetailResponse userDetailResponse = userUtil.userCall(new CreateUserRequest(requestInfo, userInfo), uri);

        return userDetailResponse.getUser().get(0);

    }

    private void enrichUser(ServiceRequest request){

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getPgrEntity().getService().getAccountId();
        String tenantId = request.getPgrEntity().getService().getTenantId();

        UserDetailResponse userDetailResponse = searchUser(userUtil.getStateLevelTenant(tenantId),accountId,null);

        if(userDetailResponse.getUser().isEmpty())
            throw new CustomException("INVALID_ACCOUNTID","No user exist for the given accountId");

        else request.getPgrEntity().getService().setCitizen(userDetailResponse.getUser().get(0));

    }

    private UserDetailResponse searchUser(String stateLevelTenant, String accountId, String userName){

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
//        userSearchRequest.setUserType(USERTYPE_CITIZEN);
        userSearchRequest.setTenantId(stateLevelTenant);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtil.userCall(userSearchRequest,uri);

    }

    private User updateUser(RequestInfo requestInfo, User user, User userFromSearch) {

        userFromSearch.setName(user.getName());

        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserUpdateEndpoint());


        UserDetailResponse userDetailResponse = userUtil.userCall(new CreateUserRequest(requestInfo, userFromSearch), uri);

        return userDetailResponse.getUser().get(0);

    }

    private void upsertUser(ServiceRequest request){

        User user = request.getPgrEntity().getService().getCitizen();
        String tenantId = request.getPgrEntity().getService().getTenantId();
        User userServiceResponse = null;

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(userUtil.getStateLevelTenant(tenantId),null, user.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            if(!user.getName().equalsIgnoreCase(userFromSearch.getName())){
                userServiceResponse = updateUser(request.getRequestInfo(),user,userFromSearch);
            }
            else userServiceResponse = userDetailResponse.getUser().get(0);
        }
        else {
            userServiceResponse = createUser(request.getRequestInfo(),tenantId,user);
        }

        // Enrich the accountId
        request.getPgrEntity().getService().setAccountId(userServiceResponse.getUuid());
    }


}