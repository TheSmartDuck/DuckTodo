package top.smartduck.ducktodo.common.constant;

import java.util.regex.Pattern;

public class SystemConstant {
    /**
     * 默认用户头像文件名
     */
    public static final String DEFAULT_USER_AVATAR = "default-user-avatar.png";

    /**
     * 默认任务族颜色
     */
    public static final String DEFAULT_GROUP_COLOR = "#5C7F71";

    public static final String DEFAULT_TEAM_COLOR = "#5C7F71";

    /**
     * 6位十六进制颜色校验（例如：#409EFF）
     */
    public static final Pattern HEX_COLOR_6 = Pattern.compile("^#[0-9a-fA-F]{6}$");
}
