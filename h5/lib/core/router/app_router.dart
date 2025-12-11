import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/auth/presentation/login_page.dart';
import '../../features/home/presentation/home_page.dart';
import '../../features/quiz/presentation/quiz_page.dart';
import '../../features/buzz/presentation/buzz_page.dart';
import '../../features/session/presentation/session_list_page.dart';
import '../../features/result/presentation/result_page.dart';
import '../providers/auth_provider.dart';

/// 路由配置Provider
final routerProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);
  
  return GoRouter(
    initialLocation: '/login',
    redirect: (context, state) {
      final isLoggedIn = authState.isLoggedIn;
      final isLoginRoute = state.matchedLocation == '/login';
      
      if (!isLoggedIn && !isLoginRoute) {
        return '/login';
      }
      
      if (isLoggedIn && isLoginRoute) {
        return '/home';
      }
      
      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/home',
        name: 'home',
        builder: (context, state) => const HomePage(),
      ),
      GoRoute(
        path: '/quiz/:sessionId',
        name: 'quiz',
        builder: (context, state) {
          final sessionId = int.parse(state.pathParameters['sessionId']!);
          return QuizPage(sessionId: sessionId);
        },
      ),
      GoRoute(
        path: '/buzz/:sessionId',
        name: 'buzz',
        builder: (context, state) {
          final sessionId = int.parse(state.pathParameters['sessionId']!);
          return BuzzPage(sessionId: sessionId);
        },
      ),
      GoRoute(
        path: '/sessions',
        name: 'sessions',
        builder: (context, state) => const SessionListPage(),
      ),
      GoRoute(
        path: '/result/:sessionId',
        name: 'result',
        builder: (context, state) {
          final sessionId = int.parse(state.pathParameters['sessionId']!);
          return ResultPage(sessionId: sessionId);
        },
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text('页面未找到', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () => context.go('/home'),
              child: const Text('返回首页'),
            ),
          ],
        ),
      ),
    ),
  );
});
