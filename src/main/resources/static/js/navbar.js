/**
 * Modern Navbar Enhancement Script
 * Adds dynamic behaviors to the navbar for better UX
 */

(function() {
    'use strict';

    // Wait for DOM to be fully loaded
    document.addEventListener('DOMContentLoaded', function() {
        
        // Get navbar element
        const navbar = document.querySelector('.modern-navbar');
        
        if (!navbar) return;

        // Add scroll effect to navbar
        let lastScrollTop = 0;
        const scrollThreshold = 50;

        function handleScroll() {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            
            // Add/remove scrolled class based on scroll position
            if (scrollTop > scrollThreshold) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
            
            lastScrollTop = scrollTop;
        }

        // Throttle scroll event for better performance
        let isScrolling = false;
        window.addEventListener('scroll', function() {
            if (!isScrolling) {
                window.requestAnimationFrame(function() {
                    handleScroll();
                    isScrolling = false;
                });
                isScrolling = true;
            }
        }, { passive: true });

        // Close mobile menu when clicking on a nav link
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
        const navbarCollapse = document.getElementById('navbarNav');
        
        if (navbarCollapse) {
            navLinks.forEach(function(link) {
                link.addEventListener('click', function() {
                    // Only close on mobile
                    if (window.innerWidth < 992) {
                        const bsCollapse = bootstrap.Collapse.getInstance(navbarCollapse);
                        if (bsCollapse) {
                            bsCollapse.hide();
                        }
                    }
                });
            });
        }

        // Add active state to current page nav link
        const currentPath = window.location.pathname;
        navLinks.forEach(function(link) {
            const linkPath = link.getAttribute('href');
            if (linkPath && currentPath.startsWith(linkPath) && linkPath !== '/') {
                link.classList.add('active');
            } else if (linkPath === '/' && currentPath === '/') {
                link.classList.add('active');
            }
        });

        // Enhance search input with focus effects
        const searchInputs = document.querySelectorAll('.search-input');
        searchInputs.forEach(function(input) {
            input.addEventListener('focus', function() {
                this.parentElement.classList.add('focused');
            });
            
            input.addEventListener('blur', function() {
                this.parentElement.classList.remove('focused');
            });
        });

        // Add ripple effect to nav links (optional enhancement)
        function createRipple(event) {
            const button = event.currentTarget;
            const ripple = document.createElement('span');
            const diameter = Math.max(button.clientWidth, button.clientHeight);
            const radius = diameter / 2;

            ripple.style.width = ripple.style.height = `${diameter}px`;
            ripple.style.left = `${event.clientX - button.offsetLeft - radius}px`;
            ripple.style.top = `${event.clientY - button.offsetTop - radius}px`;
            ripple.classList.add('ripple');

            const existingRipple = button.querySelector('.ripple');
            if (existingRipple) {
                existingRipple.remove();
            }

            button.appendChild(ripple);
        }

        // Add ripple effect CSS if not already added
        if (!document.getElementById('ripple-style')) {
            const style = document.createElement('style');
            style.id = 'ripple-style';
            style.innerHTML = `
                .nav-link-modern {
                    position: relative;
                    overflow: hidden;
                }
                .ripple {
                    position: absolute;
                    border-radius: 50%;
                    background: rgba(255, 255, 255, 0.3);
                    transform: scale(0);
                    animation: ripple-animation 0.6s ease-out;
                    pointer-events: none;
                }
                @keyframes ripple-animation {
                    to {
                        transform: scale(4);
                        opacity: 0;
                    }
                }
            `;
            document.head.appendChild(style);
        }

        // Apply ripple effect to modern nav links
        const modernNavLinks = document.querySelectorAll('.nav-link-modern');
        modernNavLinks.forEach(function(link) {
            link.addEventListener('click', createRipple);
        });

        // Cart badge animation on update
        const cartBadge = document.querySelector('.cart-badge');
        if (cartBadge) {
            // Observe changes to cart count
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'characterData' || mutation.type === 'childList') {
                        cartBadge.classList.add('badge-updated');
                        setTimeout(function() {
                            cartBadge.classList.remove('badge-updated');
                        }, 600);
                    }
                });
            });

            observer.observe(cartBadge, {
                characterData: true,
                childList: true,
                subtree: true
            });

            // Add badge update animation style
            if (!document.getElementById('badge-animation-style')) {
                const badgeStyle = document.createElement('style');
                badgeStyle.id = 'badge-animation-style';
                badgeStyle.innerHTML = `
                    .badge-updated {
                        animation: badge-bounce 0.6s ease;
                    }
                    @keyframes badge-bounce {
                        0%, 100% { transform: translate(-50%, -50%) scale(1); }
                        25% { transform: translate(-50%, -50%) scale(1.3); }
                        50% { transform: translate(-50%, -50%) scale(0.9); }
                        75% { transform: translate(-50%, -50%) scale(1.15); }
                    }
                `;
                document.head.appendChild(badgeStyle);
            }
        }

        // Initialize Bootstrap tooltips for icons (if needed)
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function(tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });

    });

})();

