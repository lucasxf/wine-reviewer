import 'package:logger/logger.dart';

/// Centralized logger for the Wine Reviewer app.
///
/// This provides structured logging with severity levels that can be filtered
/// in production. Uses the 'logger' package for better debugging and
/// production-ready logging.
///
/// **Usage:**
/// ```dart
/// AppLogger.error('Error message', error: e, stackTrace: stackTrace);
/// AppLogger.warning('Warning message');
/// AppLogger.info('Info message');
/// AppLogger.debug('Debug message');
/// ```
///
/// **Log Levels:**
/// - error: Errors that need immediate attention
/// - warning: Warning conditions that should be reviewed
/// - info: General informational messages
/// - debug: Detailed debugging information (filtered in production)
///
/// **Production Configuration:**
/// In production builds (flutter build --release), debug and verbose logs
/// are automatically filtered out by the Logger's filter level.
class AppLogger {
  static final Logger _logger = Logger(
    printer: PrettyPrinter(
      methodCount: 2, // Number of method calls to show in stack trace
      errorMethodCount: 8, // Number of method calls for errors
      lineLength: 120, // Width of output
      colors: true, // Colorful log messages
      printEmojis: true, // Print emojis for each log level
      dateTimeFormat: DateTimeFormat.onlyTimeAndSinceStart,
    ),
    filter: ProductionFilter(), // Only show errors and warnings in production
  );

  /// Log an error message with optional error and stack trace.
  static void error(
    String message, {
    Object? error,
    StackTrace? stackTrace,
  }) {
    _logger.e(message, error: error, stackTrace: stackTrace);
  }

  /// Log a warning message.
  static void warning(String message) {
    _logger.w(message);
  }

  /// Log an info message.
  static void info(String message) {
    _logger.i(message);
  }

  /// Log a debug message (filtered in production builds).
  static void debug(String message) {
    _logger.d(message);
  }
}
