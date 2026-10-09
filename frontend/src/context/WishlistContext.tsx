import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';

interface WishlistContextType {
  wishlist: number[];
  isInWishlist: (productId: number) => boolean;
  toggleWishlist: (productId: number) => void;
  wishlistCount: number;
}

const WishlistContext = createContext<
  WishlistContextType | undefined
>(undefined);

interface WishlistProviderProps {
  children: ReactNode;
}

export const WishlistProvider = ({
  children,
}: WishlistProviderProps) => {
  const [wishlist, setWishlist] = useState<number[]>([]);

  useEffect(() => {
    const storedWishlist = localStorage.getItem('wishlist');

    if (storedWishlist) {
      try {
        const parsedWishlist = JSON.parse(storedWishlist);

        if (Array.isArray(parsedWishlist)) {
          setWishlist(parsedWishlist);
        }
      } catch (error) {
        console.error(
          'Failed to load wishlist:',
          error
        );

        localStorage.removeItem('wishlist');
      }
    }
  }, []);

  useEffect(() => {
    localStorage.setItem(
      'wishlist',
      JSON.stringify(wishlist)
    );
  }, [wishlist]);

  const isInWishlist = (productId: number): boolean => {
    return wishlist.includes(productId);
  };

  const toggleWishlist = (productId: number): void => {
    setWishlist((previousWishlist) => {
      if (previousWishlist.includes(productId)) {
        return previousWishlist.filter(
          (id) => id !== productId
        );
      }

      return [...previousWishlist, productId];
    });
  };

  const wishlistCount = wishlist.length;

  return (
    <WishlistContext.Provider
      value={{
        wishlist,
        isInWishlist,
        toggleWishlist,
        wishlistCount,
      }}
    >
      {children}
    </WishlistContext.Provider>
  );
};

export const useWishlist = (): WishlistContextType => {
  const context = useContext(WishlistContext);

  if (context === undefined) {
    throw new Error(
      'useWishlist must be used within a WishlistProvider'
    );
  }

  return context;
};