package com.learn.hub.user;

import com.learn.hub.topic.ConflictException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 账号服务：注册校验、BCrypt 哈希、登录校验与 JWT 签发。
 */
@Service
public class AuthService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,32}$");

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * 注册新用户：校验用户名/密码，使用 BCrypt 存哈希，直接返回登录令牌。
     */
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String username = trim(request.username());
        String password = request.password();
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名需为 3-32 位字母、数字或下划线");
        }
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw new IllegalArgumentException("密码长度需为 6-64 位");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("USERNAME_TAKEN", "用户名已被占用");
        }
        String displayName = trim(request.displayName());
        if (displayName == null) {
            displayName = username;
        }
        if (displayName.length() > 64) {
            throw new IllegalArgumentException("昵称不能超过 64 字");
        }

        UserEntity user = userRepository.save(new UserEntity(
                username,
                passwordEncoder.encode(password),
                displayName,
                LocalDateTime.now()
        ));
        return response(user);
    }

    /**
     * 登录校验：用户名不存在与密码错误返回同一个 401，避免泄露账号是否存在。
     */
    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new UnauthorizedException("INVALID_CREDENTIALS", "用户名或密码错误");
        }
        UserEntity user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS", "用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("INVALID_CREDENTIALS", "用户名或密码错误");
        }
        return response(user);
    }

    /**
     * 读取当前用户信息；用户被删除后旧令牌会在这里失效。
     */
    @Transactional(readOnly = true)
    public AuthDtos.UserView view(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "用户不存在或已被删除"));
        return toView(user);
    }

    private AuthDtos.AuthResponse response(UserEntity user) {
        return new AuthDtos.AuthResponse(
                jwtService.issue(user.getId(), user.getUsername()),
                toView(user)
        );
    }

    private AuthDtos.UserView toView(UserEntity user) {
        return new AuthDtos.UserView(user.getId(), user.getUsername(), user.getDisplayName());
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
